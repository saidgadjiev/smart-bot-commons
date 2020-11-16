package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.BusyWorkerException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;
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
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

@Component
public class QueueJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueJob.class);

    private QueueService queueService;

    private SmartExecutorService executor;

    private FileLimitProperties fileLimitProperties;

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
    public void setExecutor(@Qualifier("queueTaskExecutor") SmartExecutorService executor) {
        this.executor = executor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", disableJobs);
        applicationEventPublisher.publishEvent(new QueueJobInitialization(this));
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
        queueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({}, {})", job.getId(), job.getWeight());
    }

    public final int removeAndCancelCurrentTasks(long chatId) {
        List<QueueItem> conversionQueueItems = queueService.deleteAndGetProcessingOrWaitingByUserId((int) chatId);
        for (QueueItem item : conversionQueueItems) {
            executor.cancelAndComplete(item.getId(), true);
            applicationEventPublisher.publishEvent(new TaskCanceled(item));
        }
        applicationEventPublisher.publishEvent(new CurrentTasksCanceled((int) chatId));

        return conversionQueueItems.size();
    }

    public void cancel(long chatId, int messageId, String queryId, int jobId) {
        QueueItem queueItem = queueService.getById(jobId);
        if (queueItem == null) {
            messageService.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(queryId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_ITEM_NOT_FOUND, userService.getLocaleOrDefault((int) chatId)))
                    .showAlert(true)
                    .build()
            );
        } else {
            messageService.sendAnswerCallbackQuery(AnswerCallbackQuery.builder().callbackQueryId(queryId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId)))
                    .build()
            );
            if (!executor.cancelAndComplete(jobId, true)) {
                queueService.deleteByIdAndStatuses(queueItem.getId(), Set.of(QueueItem.Status.WAITING, QueueItem.Status.PROCESSING));
            }
            applicationEventPublisher.publishEvent(new TaskCanceled(queueItem));
        }
        if (queueJobConfigurator.isNeedUpdateMessageAfterCancel(queueItem)) {
            messageService.editMessage(EditMessageText.builder()
                    .chatId(String.valueOf(chatId))
                    .messageId(messageId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_QUERY_CANCELED, userService.getLocaleOrDefault((int) chatId)))
                    .build(), false);
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

        private QueueWorker queueWorker;

        private QueueTask(QueueItem queueItem, QueueWorker queueWorker) {
            this.queueItem = queueItem;
            this.queueWorker = queueWorker;
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
                queueWorker.execute();
                if (!queueJobConfigurator.shouldBeDeletedAfterCompleted(queueItem)) {
                    queueService.setCompleted(queueItem.getId());
                }
                success = true;
            } catch (BusyWorkerException | FloodControlException ex) {
                queueService.setWaitingAndDecrementAttempts(queueItem.getId());
            } catch (Throwable ex) {
                if (checker == null || !checker.get()) {
                    if (FileManager.isNoneCriticalDownloadingException(ex)) {
                        handleNoneCriticalDownloadingException(ex);
                    } else {
                        queueService.setExceptionStatus(queueItem.getId(), ex);
                        queueWorker.unhandledException(ex);

                        throw ex;
                    }
                }
            } finally {
                if (checker == null || !checker.get()) {
                    executor.complete(queueItem.getId());
                    queueWorker.finish();
                    if (success && queueJobConfigurator.shouldBeDeletedAfterCompleted(queueItem)) {
                        queueService.deleteById(queueItem.getId());
                    }
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
            queueWorker.cancel();
        }

        @Override
        public String getErrorCode(Throwable e) {
            return queueJobConfigurator.getErrorCode(e);
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
            queueService.setWaitingIfThereAreAttemptsElseException(queueItem.getId(), ex);
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
            String message = queueJobConfigurator.getWaitingMessage(queueItem, locale);

            messageService.editMessage(EditMessageText.builder()
                    .chatId(String.valueOf(queueItem.getUserId()))
                    .messageId(queueItem.getProgressMessageId())
                    .text(message)
                    .replyMarkup(queueJobConfigurator.getWaitingKeyboard(queueItem, locale))
                    .build());
        }
    }
}
