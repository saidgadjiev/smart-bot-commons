package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.message.ForceMediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.UploadCompleted;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class UploadJob extends WorkQueueJobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadJob.class);

    private UploadQueueService uploadQueueService;

    private FileManagerProperties fileManagerProperties;

    private MediaLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    private FileUploader fileUploader;

    private SmartExecutorService uploadTasksExecutor;

    private ApplicationEventPublisher applicationEventPublisher;

    private final List<UploadQueueItem> currentUploads = new ArrayList<>();

    private MessageService messageService;

    private UserService userService;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Value("${enable.jobs.logging:false}")
    private boolean enableJobsLogging;

    @Autowired
    public UploadJob(UploadQueueService uploadQueueService,
                     FileManagerProperties fileManagerProperties,
                     MediaLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao, ApplicationEventPublisher applicationEventPublisher,
                     FileUploader fileUploader, @Qualifier("messageLimits") MessageService messageService, UserService userService) {
        this.uploadQueueService = uploadQueueService;
        this.fileManagerProperties = fileManagerProperties;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fileUploader = fileUploader;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Autowired
    public void setUploadTasksExecutor(@Qualifier("uploadTasksExecutor") SmartExecutorService uploadTasksExecutor) {
        this.uploadTasksExecutor = uploadTasksExecutor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", disableJobs);
        LOGGER.debug("Enable jobs logging {}", enableJobsLogging);
        try {
            uploadQueueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        doUploads();
    }

    public void rejectTask(SmartExecutorService.Job job) {
        uploadQueueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({})", job.getId());
    }

    @Scheduled(fixedDelay = 1000)
    public void doUploads() {
        super.push();
    }

    @Override
    public SmartExecutorService getExecutor() {
        return uploadTasksExecutor;
    }

    @Override
    public boolean enableJobsLogging() {
        return enableJobsLogging;
    }

    @Override
    public boolean disableJobs() {
        return disableJobs;
    }

    @Override
    public Class<?> getLoggerClass() {
        return getClass();
    }

    @Override
    public List<QueueItem> getTasks(SmartExecutorService.JobWeight weight, int limit) {
        return (List<QueueItem>) (Object) uploadQueueService.poll(workQueueDao.getProducerName(), weight, limit);
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem item) {
        return new UploadTask((UploadQueueItem) item);
    }

    public void cancelUploads(String producer, int producerId) {
        cancelUploads(producer, Set.of(producerId));
    }

    public void cancelUploads(String producer, Set<Integer> producerIds) {
        deleteUploads(producer, producerIds);
    }

    public void deleteUploads(String producer, Set<Integer> producerIds) {
        List<UploadQueueItem> deleted = uploadQueueService.deleteByProducerIdsWithReturning(producer, producerIds);
        uploadTasksExecutor.cancel(deleted.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), true);
        releaseResources(deleted);
    }

    public void cleanUpUploads(String producer, Set<Integer> producerIds) {
        List<UploadQueueItem> deleted = new ArrayList<>(uploadQueueService.deleteByProducerIdsWithReturning(producer, producerIds));
        List<UploadQueueItem> orphanUploads = uploadQueueService.deleteOrphanUploads(producer);
        deleted.addAll(orphanUploads);
        uploadTasksExecutor.cancel(deleted.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), true);
        releaseResources(deleted);
    }

    public void cancelUploads() {
        uploadTasksExecutor.cancel(currentUploads.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), false);
    }

    public final void shutdown() {
        uploadTasksExecutor.shutdown();
    }

    private void releaseResources(List<UploadQueueItem> uploadQueueItems) {
        for (UploadQueueItem uploadQueueItem : uploadQueueItems) {
            releaseResources(uploadQueueItem);
        }
    }

    private void releaseResources(UploadQueueItem uploadQueueItem) {
        if (uploadQueueItem == null) {
            return;
        }
        InputFile inputFile = null;
        InputFile thumb = null;
        switch (uploadQueueItem.getMethod()) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) uploadQueueItem.getBody();
                inputFile = sendDocument.getDocument();
                thumb = sendDocument.getThumb();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) uploadQueueItem.getBody();
                inputFile = sendAudio.getAudio();
                thumb = sendAudio.getThumb();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) uploadQueueItem.getBody();
                inputFile = sendVideo.getVideo();
                thumb = sendVideo.getThumb();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) uploadQueueItem.getBody();
                inputFile = sendVoice.getVoice();
                break;
            }
        }

        if (inputFile != null && inputFile.isNew()) {
            new SmartTempFile(inputFile.getNewMediaFile()).smartDelete();
        }
        if (thumb != null && thumb.isNew()) {
            new SmartTempFile(thumb.getNewMediaFile()).smartDelete();
        }
    }

    private class UploadTask implements SmartExecutorService.Job {

        private UploadQueueItem uploadQueueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private UploadTask(UploadQueueItem uploadQueueItem) {
            this.uploadQueueItem = uploadQueueItem;
        }

        @Override
        public void execute() {
            currentUploads.add(uploadQueueItem);
            try {
                SendFileResult sendFileResult = fileUploader.upload(uploadQueueItem.getMethod(), uploadQueueItem.getBody(), uploadQueueItem.getProgress());
                uploadQueueService.setCompleted(uploadQueueItem.getId());
                releaseResources(uploadQueueItem);

                applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
            } catch (Exception e) {
                if (checker == null || !checker.get()) {
                    if (e instanceof FloodControlException) {
                        floodControlException(uploadQueueItem, (FloodControlException) e);
                    } else if (e instanceof FloodWaitException) {
                        floodWaitException(uploadQueueItem, (FloodWaitException) e);
                    } else if (ForceMediaMessageService.shouldTryToUploadAgain(e)) {
                        noneCriticalException(uploadQueueItem, e);
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        uploadQueueService.setExceptionStatus(uploadQueueItem.getId(), e);
                        messageService.sendErrorMessage(uploadQueueItem.getUserId(), userService.getLocaleOrDefault(uploadQueueItem.getUserId()));
                    }
                }
            } finally {
                currentUploads.remove(uploadQueueItem);
            }
        }

        @Override
        public int getId() {
            return uploadQueueItem.getId();
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return uploadQueueItem.getFileSize() > mediaLimitProperties.getLightFileMaxWeight() ? SmartExecutorService.JobWeight.HEAVY : SmartExecutorService.JobWeight.LIGHT;
        }

        @Override
        public long getChatId() {
            return uploadQueueItem.getUserId();
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
        public void cancel() {
            fileUploader.cancelUploading(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
            if (canceledByUser) {
                uploadQueueService.deleteById(uploadQueueItem.getId());
                releaseResources(uploadQueueItem);
                LOGGER.debug("Canceled upload({}, {}, {})", uploadQueueItem.getMethod(), uploadQueueItem.getProducerTable(), uploadQueueItem.getProducerId());
            }
        }

        private void noneCriticalException(UploadQueueItem uploadQueueItem, Throwable e) {
            uploadQueueService.setWaitingAndDecrementAttempts(uploadQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeUploadAttempt(), e);
        }

        private void floodControlException(UploadQueueItem uploadQueueItem, FloodControlException e) {
            uploadQueueService.setWaitingAndDecrementAttempts(uploadQueueItem.getId(), e.getSleepTime(), e);
        }

        private void floodWaitException(UploadQueueItem uploadQueueItem, FloodWaitException e) {
            uploadQueueService.setWaiting(uploadQueueItem.getId(), e.getSleepTime(), e);
        }
    }
}
