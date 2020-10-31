package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.BusyWorkerException;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileWorkObject;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobDelegate;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

@Component
public class QueueJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueJob.class);

    private QueueService queueService;

    private SmartExecutorService executor;

    private FileLimitProperties fileLimitProperties;

    private FileManager fileManager;

    private QueueJobDelegate queueWorker;

    private UserService userService;

    private MessageService messageService;

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    @Autowired
    public void setMessageService(@Qualifier("messageLimits") MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setFileLimitProperties(FileLimitProperties fileLimitProperties) {
        this.fileLimitProperties = fileLimitProperties;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Autowired
    public void setQueueWorker(QueueJobDelegate queueWorker) {
        this.queueWorker = queueWorker;
    }

    @Autowired
    public void setExecutor(@Qualifier("queueTaskExecutor") SmartExecutorService executor) {
        this.executor = executor;
    }

    @PostConstruct
    public final void init() {
        queueWorker.init();
        try {
            queueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        pushJobs();
    }

    @Scheduled(fixedDelay = 5000)
    public final void pushJobs() {
        ThreadPoolExecutor heavyExecutor = executor.getExecutor(SmartExecutorService.JobWeight.HEAVY);
        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.HEAVY, heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push heavy jobs({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorker.mapWorker(queueItem))));
        }
        ThreadPoolExecutor lightExecutor = executor.getExecutor(SmartExecutorService.JobWeight.LIGHT);
        if (lightExecutor.getActiveCount() < lightExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.LIGHT, lightExecutor.getCorePoolSize() - lightExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push light jobs({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorker.mapWorker(queueItem))));
        }
        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.LIGHT, heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push light jobs to heavy threads({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorker.mapWorker(queueItem)), SmartExecutorService.JobWeight.HEAVY));
        }
    }

    public final void rejectTask(SmartExecutorService.Job job) {
        queueService.setWaiting(job.getId());
        LOGGER.debug("Rejected({}, {})", job.getId(), job.getWeight());
    }

    public final int removeAndCancelCurrentTasks(long chatId) {
        List<QueueItem> conversionQueueItems = queueService.deleteAndGetProcessingOrWaitingByUserId((int) chatId);
        for (QueueItem conversionQueueItem : conversionQueueItems) {
            if (!executor.cancelAndComplete(conversionQueueItem.getId(), true)) {
                fileManager.fileWorkObject(conversionQueueItem.getUserId(), conversionQueueItem.getSize()).stop();
            }
            queueWorker.afterTaskCanceled(conversionQueueItem.getId());
        }

        return conversionQueueItems.size();
    }

    public final boolean cancel(int jobId) {
        QueueItem item = queueService.deleteAndGetById(jobId);

        if (item == null) {
            return false;
        }
        if (!executor.cancelAndComplete(jobId, true)) {
            fileManager.fileWorkObject(item.getId(), item.getSize()).stop();
        }
        queueWorker.afterTaskCanceled(jobId);

        return item.getStatus() != QueueItem.Status.COMPLETED;
    }

    public final void shutdown() {
        executor.shutdown();
        queueWorker.shutdown();
    }

    public class QueueTask implements SmartExecutorService.Job {

        private final QueueItem queueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private FileWorkObject fileWorkObject;

        private QueueJobDelegate.WorkerTaskDelegate workerTaskDelegate;

        private QueueTask(QueueItem queueItem, QueueJobDelegate.WorkerTaskDelegate workerTaskDelegate) {
            this.queueItem = queueItem;
            this.workerTaskDelegate = workerTaskDelegate;
            this.fileWorkObject = fileManager.fileWorkObject(queueItem.getUserId(), queueItem.getSize());
        }

        @Override
        public Integer getReplyToMessageId() {
            return queueItem.getReplyToMessageId();
        }

        @Override
        public boolean isSuppressUserExceptions() {
            return queueItem.isSuppressUserExceptions();
        }

        @Override
        public void execute() throws Exception {
            try {
                fileWorkObject.start();
                workerTaskDelegate.execute();
                queueService.setCompleted(queueItem.getId());
            } catch (BusyWorkerException ex) {
                queueService.setWaiting(queueItem.getId());
            } catch (Throwable ex) {
                if (checker == null || !checker.get()) {
                    if (FileManager.isNoneCriticalDownloadingException(ex)) {
                        handleNoneCriticalDownloadingException(ex);
                    } else {
                        queueService.setException(queueItem.getId(), ex);

                        throw ex;
                    }
                }
            } finally {
                if (checker == null || !checker.get()) {
                    executor.complete(queueItem.getId());
                    fileWorkObject.stop();
                    workerTaskDelegate.finish();
                }
            }
        }

        @Override
        public void cancel() {
            if (canceledByUser) {
                queueService.deleteById(queueItem.getId());
                LOGGER.debug("Canceled({}, {}, {})", queueItem.getUserId(), queueItem.getId(), MemoryUtils.humanReadableByteCount(queueItem.getSize()));
            }
            executor.complete(queueItem.getId());
            fileWorkObject.stop();
            workerTaskDelegate.cancel();
        }

        @Override
        public String getErrorCode(Throwable e) {
            return workerTaskDelegate.getErrorCode(e);
        }

        @Override
        public int getId() {
            return queueItem.getId();
        }

        @Override
        public void setCancelChecker(Supplier<Boolean> checker) {
            this.checker = checker;
        }

        @Override
        public Supplier<Boolean> getCancelChecker() {
            return checker;
        }

        @Override
        public void setCanceledByUser(boolean canceledByUser) {
            this.canceledByUser = canceledByUser;
        }

        @Override
        public boolean isCanceledByUser() {
            return canceledByUser;
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return queueItem.getSize() > fileLimitProperties.getLightFileMaxWeight() ? SmartExecutorService.JobWeight.HEAVY : SmartExecutorService.JobWeight.LIGHT;
        }

        @Override
        public long getChatId() {
            return queueItem.getUserId();
        }

        @Override
        public int getProgressMessageId() {
            return queueItem.getProgressMessageId();
        }

        private void handleNoneCriticalDownloadingException(Throwable ex) {
            queueService.setWaiting(queueItem.getId(), ex);
            if (!FileManager.isNoneCriticalDownloadingException(queueItem.getException())) {
                updateProgressMessageAfterNoneCriticalException(queueItem.getId());
            }
        }

        private void updateProgressMessageAfterNoneCriticalException(int id) {
            QueueItem queueItem = queueService.getById(id);

            if (queueItem == null) {
                return;
            }
            Locale locale = userService.getLocaleOrDefault(queueItem.getUserId());
            String message = workerTaskDelegate.getWaitingMessage(queueItem, locale);

            messageService.editMessage(new EditMessageText((long) queueItem.getUserId(), queueItem.getProgressMessageId(), message)
                    .setNoLogging(true)
                    .setReplyMarkup(workerTaskDelegate.getWaitingKeyboard(queueItem, locale)));
        }
    }
}
