package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
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
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.message.ForceMediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.event.UploadCompleted;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class UploadJob extends JobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadJob.class);

    private MediaMessageService mediaMessageService;

    private UploadQueueService uploadQueueService;

    private FileManagerProperties fileManagerProperties;

    private WorkQueueDao workQueueDao;

    private SmartExecutorService uploadTasksExecutor;

    private ApplicationEventPublisher applicationEventPublisher;

    private final List<UploadQueueItem> currentUploads = new ArrayList<>();

    private FileUploader fileUploader;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Value("${enable.jobs.logging:false}")
    private boolean enableJobsLogging;

    @Autowired
    public UploadJob(@Qualifier("media") MediaMessageService mediaMessageService, UploadQueueService uploadQueueService,
                     FileManagerProperties fileManagerProperties,
                     WorkQueueDao workQueueDao, ApplicationEventPublisher applicationEventPublisher, FileUploader fileUploader) {
        this.mediaMessageService = mediaMessageService;
        this.uploadQueueService = uploadQueueService;
        this.fileManagerProperties = fileManagerProperties;
        this.workQueueDao = workQueueDao;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fileUploader = fileUploader;
    }

    @Autowired
    public void setUploadTasksExecutor(@Qualifier("uploadTasksExecutor") SmartExecutorService uploadTasksExecutor) {
        this.uploadTasksExecutor = uploadTasksExecutor;
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
        return (List<QueueItem>) (Object) uploadQueueService.poll(workQueueDao.getQueueName(), limit);
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem item) {
        return new UploadTask((UploadQueueItem) item);
    }

    public void cancelUploads(String producer, int producerId) {
        cancelUploads(producer, Set.of(producerId));
    }

    public void cancelUploads(String producer, Set<Integer> producerIds) {
        List<UploadQueueItem> uploads = uploadQueueService.getUploads(producer, producerIds);

        uploadTasksExecutor.cancelAndComplete(uploads.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), true);
        uploadQueueService.deleteByProducer(producer, producerIds);
    }

    public void cancelUploads() {
        uploadTasksExecutor.cancelAndComplete(currentUploads.stream().map(UploadQueueItem::getId).collect(Collectors.toList()), false);
    }

    public final void shutdown() {
        uploadTasksExecutor.shutdown();
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
                doUpload();
                uploadQueueService.setCompleted(uploadQueueItem.getId());
            } catch (Exception e) {
                if (checker == null || !checker.get()) {
                    if (e instanceof FloodWaitException) {
                        floodWaitException(uploadQueueItem, (FloodWaitException) e);
                    } else if (ForceMediaMessageService.shouldTryToUploadAgain(e)) {
                        noneCriticalException(uploadQueueItem, e);
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        uploadQueueService.setExceptionStatus(uploadQueueItem.getId(), e);
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
            return SmartExecutorService.JobWeight.HEAVY;
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
            String filePath = getFilePath();
            if (StringUtils.isNotBlank(filePath)) {
                fileUploader.cancelUploading(filePath);
            }
            if (canceledByUser) {
                uploadQueueService.deleteById(uploadQueueItem.getId());
                LOGGER.debug("Canceled upload({}, {}, {})", uploadQueueItem.getMethod(), uploadQueueItem.getProducer(), uploadQueueItem.getProducerId());
            }
        }

        private String getFilePath() {
            InputFile inputFile = null;
            switch (uploadQueueItem.getMethod()) {
                case SendDocument.PATH: {
                    SendDocument sendDocument = (SendDocument) uploadQueueItem.getBody();
                    inputFile = sendDocument.getDocument();
                    break;
                }
                case SendAudio.PATH: {
                    SendAudio sendAudio = (SendAudio) uploadQueueItem.getBody();
                    inputFile = sendAudio.getAudio();
                    break;
                }
                case SendVideo.PATH: {
                    SendVideo sendVideo = (SendVideo) uploadQueueItem.getBody();
                    inputFile = sendVideo.getVideo();
                    break;
                }
                case SendVoice.PATH: {
                    SendVoice sendVoice = (SendVoice) uploadQueueItem.getBody();
                    inputFile = sendVoice.getVoice();
                    break;
                }
            }

            if (inputFile != null && inputFile.isNew()) {
                return inputFile.getNewMediaFile().getAbsolutePath();
            }

            return null;
        }

        private void doUpload() {
            switch (uploadQueueItem.getMethod()) {
                case SendDocument.PATH: {
                    SendDocument sendDocument = (SendDocument) uploadQueueItem.getBody();
                    SendFileResult sendFileResult = mediaMessageService.sendDocument(sendDocument, uploadQueueItem.getProgress());
                    applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
                    break;
                }
                case SendAudio.PATH: {
                    SendAudio sendAudio = (SendAudio) uploadQueueItem.getBody();
                    SendFileResult sendFileResult = mediaMessageService.sendAudio(sendAudio, uploadQueueItem.getProgress());
                    applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
                    break;
                }
                case SendVideo.PATH: {
                    SendVideo sendVideo = (SendVideo) uploadQueueItem.getBody();
                    SendFileResult sendFileResult = mediaMessageService.sendVideo(sendVideo, uploadQueueItem.getProgress());
                    applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
                    break;
                }
                case SendVoice.PATH: {
                    SendVoice sendVoice = (SendVoice) uploadQueueItem.getBody();
                    SendFileResult sendFileResult = mediaMessageService.sendVoice(sendVoice, uploadQueueItem.getProgress());
                    applicationEventPublisher.publishEvent(new UploadCompleted(sendFileResult, uploadQueueItem));
                    break;
                }
            }
        }

        private void noneCriticalException(UploadQueueItem uploadQueueItem, Throwable e) {
            uploadQueueService.setWaiting(uploadQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeUploadAttempt(), e);
        }

        private void floodWaitException(UploadQueueItem uploadQueueItem, FloodWaitException e) {
            uploadQueueService.setWaiting(uploadQueueItem.getId(), e.getSleepTime(), e);
        }
    }
}
