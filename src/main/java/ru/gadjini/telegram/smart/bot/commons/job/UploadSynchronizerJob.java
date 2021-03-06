package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadSynchronizerService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.io.File;
import java.util.List;

@Component
@Profile({SmartBotConfiguration.PROFILE_PROD_PRIMARY, SmartBotConfiguration.PROFILE_DEV_PRIMARY})
public class UploadSynchronizerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSynchronizerJob.class);

    private UploadSynchronizerService uploadSynchronizerService;

    private FileUploader fileUploader;

    private WorkQueueService workQueueService;

    @Autowired
    public UploadSynchronizerJob(UploadSynchronizerService uploadSynchronizerService,
                                 FileUploader fileUploader, WorkQueueService queueService) {
        this.uploadSynchronizerService = uploadSynchronizerService;
        this.fileUploader = fileUploader;
        this.workQueueService = queueService;
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void doSynchronize() {
        String producer = ((WorkQueueDao) workQueueService.getQueueDao()).getProducerName();
        List<UploadQueueItem> unsynchronizedUploads = uploadSynchronizerService.getUnsynchronizedUploads(producer);

        for (UploadQueueItem unsynchronizedUpload : unsynchronizedUploads) {
            try {
                synchronize(unsynchronizedUpload);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
    }

    private void synchronize(UploadQueueItem uploadQueueItem) {
        if (isFullySynchronized(uploadQueueItem)) {
            uploadSynchronizerService.synchronize(uploadQueueItem.getId());
        }
    }

    private boolean isFullySynchronized(UploadQueueItem uploadQueueItem) {
        InputFile inputFile = fileUploader.getInputFile(uploadQueueItem.getMethod(), uploadQueueItem.getBody());
        if (inputFile.isNew()) {
            File file = inputFile.getNewMediaFile();

            return file.exists() && file.length() == uploadQueueItem.getFileSize();
        }

        return true;
    }
}
