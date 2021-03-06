package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadSynchronizerService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.io.File;
import java.util.List;

@Component
@Profile({SmartBotConfiguration.PROFILE_PROD_SECONDARY, SmartBotConfiguration.PROFILE_DEV_SECONDARY})
public class DownloadSynchronizerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadSynchronizerJob.class);

    private DownloadSynchronizerService downloadSynchronizerService;

    private WorkQueueService workQueueService;

    @Autowired
    public DownloadSynchronizerJob(DownloadSynchronizerService downloadSynchronizerService, WorkQueueService queueService) {
        this.downloadSynchronizerService = downloadSynchronizerService;
        this.workQueueService = queueService;
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void doSynchronize() {
        String producer = ((WorkQueueDao) workQueueService.getQueueDao()).getProducerName();
        List<DownloadQueueItem> unsynchronizedDownloads = downloadSynchronizerService.getUnsynchronizedDownloads(producer);
        for (DownloadQueueItem unsynchronizedDownload : unsynchronizedDownloads) {
            try {
                synchronize(unsynchronizedDownload);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void synchronize(DownloadQueueItem downloadQueueItem) {
        if (isFullySynchronized(downloadQueueItem)) {
            downloadSynchronizerService.synchronize(downloadQueueItem.getId());
        }
    }

    private boolean isFullySynchronized(DownloadQueueItem downloadQueueItem) {
        if (StringUtils.isBlank(downloadQueueItem.getFilePath())) {
            return true;
        }
        File file = new File(downloadQueueItem.getFilePath());
        return file.exists() && (file.length() == downloadQueueItem.getFile().getSize()
                || downloadQueueItem.getFile().getSize() == 0); //May be on old thumb
    }
}
