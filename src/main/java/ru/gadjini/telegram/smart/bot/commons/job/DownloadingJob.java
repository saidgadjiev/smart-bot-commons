package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloader;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadingQueueService;

import java.io.File;
import java.util.List;

@Component
public class DownloadingJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadingJob.class);

    private static final String TAG = "down";

    private DownloadingQueueService downloadingQueueService;

    private FileDownloader fileDownloader;

    private TempFileService tempFileService;

    private FileManagerProperties fileManagerProperties;

    private WorkQueueDao workQueueDao;

    private MessageService messageService;

    private UserService userService;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Value("${enable.jobs.logging:false}")
    private boolean enableJobsLogging;

    @Autowired
    public DownloadingJob(DownloadingQueueService downloadingQueueService, FileDownloader fileDownloader,
                          TempFileService tempFileService, FileManagerProperties fileManagerProperties,
                          WorkQueueDao workQueueDao, @Qualifier("messageLimits") MessageService messageService, UserService userService) {
        this.downloadingQueueService = downloadingQueueService;
        this.fileDownloader = fileDownloader;
        this.tempFileService = tempFileService;
        this.fileManagerProperties = fileManagerProperties;
        this.workQueueDao = workQueueDao;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 5000)
    public void doDownloads() {
        if (disableJobs) {
            if (enableJobsLogging) {
                LOGGER.debug("Job disabled");
            }
            return;
        }
        List<DownloadingQueueItem> poll = downloadingQueueService.poll(workQueueDao.getQueueName());

        for (DownloadingQueueItem queueItem : poll) {
            doDownload(queueItem);
        }
    }

    private void doDownload(DownloadingQueueItem downloadingQueueItem) {
        if (downloadingQueueItem.getFile().getFormat().isDownloadable()) {
            doDownloadFile(downloadingQueueItem);
        } else {
            downloadingQueueService.setCompleted(downloadingQueueItem.getId());
        }
    }

    private void doDownloadFile(DownloadingQueueItem downloadingQueueItem) {
        SmartTempFile tempFile;
        if (StringUtils.isBlank(downloadingQueueItem.getFilePath())) {
            tempFile = tempFileService.createTempFile(downloadingQueueItem.getUserId(), downloadingQueueItem.getFile().getFileId(), TAG, downloadingQueueItem.getFile().getFormat().getExt());
        } else {
            tempFile = new SmartTempFile(new File(downloadingQueueItem.getFilePath()));
        }
        try {
            fileDownloader.downloadFileByFileId(downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getFile().getSize(), downloadingQueueItem.getProgress(), tempFile);
            downloadingQueueService.setCompleted(downloadingQueueItem.getId(), tempFile.getAbsolutePath());
        } catch (Exception e) {
            tempFile.smartDelete();

            if (e instanceof FloodControlException) {
                floodControlException(downloadingQueueItem, (FloodControlException) e);
            } else if (e instanceof FloodWaitException) {
                floodWaitException(downloadingQueueItem, (FloodWaitException) e);
            } else if (FileDownloader.isNoneCriticalDownloadingException(e)) {
                noneCriticalException(downloadingQueueItem, e);
            } else {
                LOGGER.error(e.getMessage(), e);
                downloadingQueueService.setExceptionStatus(downloadingQueueItem.getId(), e);
                messageService.sendErrorMessage(downloadingQueueItem.getUserId(), userService.getLocaleOrDefault(downloadingQueueItem.getUserId()));
            }
        }
    }

    private void noneCriticalException(DownloadingQueueItem downloadingQueueItem, Throwable e) {
        downloadingQueueService.setWaiting(downloadingQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeDownloadAttempt(), e);
    }

    private void floodControlException(DownloadingQueueItem downloadingQueueItem, FloodControlException e) {
        downloadingQueueService.setWaiting(downloadingQueueItem.getId(), e.getSleepTime(), e);
    }

    private void floodWaitException(DownloadingQueueItem downloadingQueueItem, FloodWaitException e) {
        downloadingQueueService.setWaiting(downloadingQueueItem.getId(), e.getSleepTime(), e);
    }
}
