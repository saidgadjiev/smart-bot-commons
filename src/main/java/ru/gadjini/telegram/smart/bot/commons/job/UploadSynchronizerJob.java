package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadSynchronizerService;

import java.io.File;
import java.util.List;

@Component
@Profile({SmartBotConfiguration.PROFILE_PROD_PRIMARY, SmartBotConfiguration.PROFILE_DEV_PRIMARY})
public class UploadSynchronizerJob {

    private UploadSynchronizerService uploadSynchronizerService;

    private FileUploader fileUploader;

    @Autowired
    public UploadSynchronizerJob(UploadSynchronizerService uploadSynchronizerService, FileUploader fileUploader) {
        this.uploadSynchronizerService = uploadSynchronizerService;
        this.fileUploader = fileUploader;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void doSynchronize() {
        List<UploadQueueItem> unsynchronizedUploads = uploadSynchronizerService.getUnsynchronizedUploads();

        for (UploadQueueItem unsynchronizedUpload : unsynchronizedUploads) {
            synchronize(unsynchronizedUpload);
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
