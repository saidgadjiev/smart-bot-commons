package ru.gadjini.telegram.smart.bot.commons.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadingJob;
import ru.gadjini.telegram.smart.bot.commons.job.QueueJob;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloadService;

@Component
public class ContextCloseListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextCloseListener.class);

    private QueueJob conversionJob;

    private DownloadingJob downloadingJob;

    private UploadJob uploadJob;

    private FileDownloadService fileManager;

    @Autowired
    public ContextCloseListener(QueueJob conversionJob, DownloadingJob downloadingJob,
                                UploadJob uploadJob, FileDownloadService fileManager) {
        this.conversionJob = conversionJob;
        this.downloadingJob = downloadingJob;
        this.uploadJob = uploadJob;
        this.fileManager = fileManager;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            conversionJob.shutdown();
        } catch (Throwable e) {
            LOGGER.error("Error shutdown conversionService. " + e.getMessage(), e);
        }
        try {
            downloadingJob.shutdown();
        } catch (Throwable e) {
            LOGGER.error("Error cancel downloading job. " + e.getMessage(), e);
        }
        try {
            uploadJob.shutdown();
        } catch (Throwable e) {
            LOGGER.error("Error cancel upload job. " + e.getMessage(), e);
        }
        try {
            fileManager.cancelDownloads();
        } catch (Throwable e) {
            LOGGER.error("Error cancel downloading telegramService. " + e.getMessage(), e);
        }
        LOGGER.debug("Shutdown success");
    }
}