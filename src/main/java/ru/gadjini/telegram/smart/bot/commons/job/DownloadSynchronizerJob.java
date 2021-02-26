package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadSynchronizerService;

import java.io.File;
import java.util.List;

@Component
@Profile(SmartBotConfiguration.PROFILE_PROD_SECONDARY)
public class DownloadSynchronizerJob {

    private DownloadSynchronizerService downloadSynchronizerService;

    @Autowired
    public DownloadSynchronizerJob(DownloadSynchronizerService downloadSynchronizerService) {
        this.downloadSynchronizerService = downloadSynchronizerService;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void doSynchronize() {
        List<DownloadQueueItem> unsynchronizedDownloads = downloadSynchronizerService.getUnsynchronizedDownloads();
        for (DownloadQueueItem unsynchronizedDownload : unsynchronizedDownloads) {
            synchronize(unsynchronizedDownload);
        }
    }

    private void synchronize(DownloadQueueItem downloadQueueItem) {
        if (isFullySynchronized(downloadQueueItem)) {
            downloadSynchronizerService.synchronize(downloadQueueItem.getId());
        }
    }

    private boolean isFullySynchronized(DownloadQueueItem downloadQueueItem) {
        File file = new File(downloadQueueItem.getFilePath());
        return file.exists() && file.length() == downloadQueueItem.getFile().getSize();
    }
}
