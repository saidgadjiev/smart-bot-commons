package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.BusyWorkerException;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.AnswerCallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileWorkObject;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueWorker;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueWorkerFactory;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.CurrentTasksCanceled;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.QueueJobInitialization;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.QueueJobShuttingDown;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.TaskCanceled;
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

    private UserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private QueueWorkerFactory queueWorkerFactory;

    private ApplicationEventPublisher applicationEventPublisher;

    private QueueJobConfigurator queueJobConfigurator;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Autowired
    public void setQueueJobConfigurator(QueueJobConfigurator queueJobConfigurator) {
        this.queueJobConfigurator = queueJobConfigurator;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setQueueWorkerFactory(QueueWorkerFactory queueWorkerFactory) {
        this.queueWorkerFactory = queueWorkerFactory;
    }

    @Autowired
    public void setLocalisationService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

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
    public void setExecutor(@Qualifier("queueTaskExecutor") SmartExecutorService executor) {
        this.executor = executor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", disableJobs);
        applicationEventPublisher.publishEvent(new QueueJobInitialization());
        try {
            queueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        pushJobs();
    }

    @Scheduled(fixedDelay = 5000)
    public final void pushJobs() {
        if (disableJobs) {
            return;
        }
        ThreadPoolExecutor heavyExecutor = executor.getExecutor(SmartExecutorService.JobWeight.HEAVY);
        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.HEAVY, heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push heavy jobs({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorkerFactory.createWorker(queueItem))));
        }
        ThreadPoolExecutor lightExecutor = executor.getExecutor(SmartExecutorService.JobWeight.LIGHT);
        if (lightExecutor.getActiveCount() < lightExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.LIGHT, lightExecutor.getCorePoolSize() - lightExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push light jobs({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorkerFactory.createWorker(queueItem))));
        }
        if (heavyExecutor.getActiveCount() < heavyExecutor.getCorePoolSize()) {
            Collection<QueueItem> items = queueService.poll(SmartExecutorService.JobWeight.LIGHT, heavyExecutor.getCorePoolSize() - heavyExecutor.getActiveCount());

            if (items.size() > 0) {
                LOGGER.debug("Push light jobs to heavy threads({})", items.size());
            }
            items.forEach(queueItem -> executor.execute(new QueueTask(queueItem, queueWorkerFactory.createWorker(queueItem)), SmartExecutorService.JobWeight.HEAVY));
        }
    }

    public final void rejectTask(SmartExecutorService.Job job) {
        queueService.setWaiting(job.getId());
        LOGGER.debug("Rejected({}, {})", job.getId(), job.getWeight());
    }

    public final int removeAndCancelCurrentTasks(long chatId) {
        List<QueueItem> conversionQueueItems = queueService.deleteAndGetProcessingOrWaitingByUserId((int) chatId);
        for (QueueItem item : conversionQueueItems) {
            if (!executor.cancelAndComplete(item.getId(), true)) {
                fileManager.fileWorkObject(item.getUserId(), item.getSize()).stop();
            }
            applicationEventPublisher.publishEvent(new TaskCanceled(item));
        }
        applicationEventPublisher.publishEvent(new CurrentTasksCanceled((int) chatId));

        return conversionQueueItems.size();
    }

    public void cancel(long chatId, int messageId, String queryId, int jobId) {
        QueueItem queueItem = queueService.getById(jobId);
        if (queueItem == null) {
            messageService.sendAnswerCallbackQuery(new AnswerCallbackQuery(
                    queryId,
                    localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_ITEM_NOT_FOUND, userService.getLocaleOrDefault((int) chatId)),
                    true
            ));
        } else {
            messageService.sendAnswerCallbackQuery(new AnswerCallbackQuery(
                    queryId,
                    localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId))
            ));
            if (!executor.cancelAndComplete(jobId, true)) {
                fileManager.fileWorkObject(queueItem.getId(), queueItem.getSize()).stop();
            }
            applicationEventPublisher.publishEvent(new TaskCanceled(queueItem));
        }
        if (queueJobConfigurator.isNeedUpdateMessageAfterCancel(queueItem)) {
            messageService.editMessage(new EditMessageText(
                    chatId, messageId, localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId))));
        }
    }

    public final void shutdown() {
        executor.shutdown();
        applicationEventPublisher.publishEvent(new QueueJobShuttingDown());
    }

    public class QueueTask implements SmartExecutorService.Job {

        private final QueueItem queueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private FileWorkObject fileWorkObject;

        private QueueWorker workerTaskDelegate;

        private QueueTask(QueueItem queueItem, QueueWorker queueWorker) {
            this.queueItem = queueItem;
            this.workerTaskDelegate = queueWorker;
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
            boolean success = false;
            try {
                fileWorkObject.start();
                workerTaskDelegate.execute();
                if (!workerTaskDelegate.shouldBeDeletedAfterCompleted()) {
                    queueService.setCompleted(queueItem.getId());
                }
                success = true;
            } catch (BusyWorkerException ex) {
                queueService.setWaiting(queueItem.getId());
            } catch (Throwable ex) {
                if (checker == null || !checker.get()) {
                    if (FileManager.isNoneCriticalDownloadingException(ex)) {
                        handleNoneCriticalDownloadingException(ex);
                    } else {
                        queueService.setException(queueItem.getId(), ex);
                        workerTaskDelegate.unhandledException(ex);

                        throw ex;
                    }
                }
            } finally {
                if (checker == null || !checker.get()) {
                    executor.complete(queueItem.getId());
                    workerTaskDelegate.finish();
                    if (success) {
                        fileWorkObject.stop();
                        if (workerTaskDelegate.shouldBeDeletedAfterCompleted()) {
                            queueService.deleteById(queueItem.getId());
                        }
                    }
                }
            }
        }

        @Override
        public void cancel() {
            if (canceledByUser) {
                queueService.deleteById(queueItem.getId());
                fileWorkObject.stop();
                LOGGER.debug("Canceled({}, {}, {})", queueItem.getUserId(), queueItem.getId(), MemoryUtils.humanReadableByteCount(queueItem.getSize()));
            }
            executor.complete(queueItem.getId());
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
