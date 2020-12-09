package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompletedItemsCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletedItemsCleaner.class);

    private WorkQueueService workQueueService;

    private DownloadJob downloadJob;

    private UploadJob uploadJob;

    @Autowired
    public CompletedItemsCleaner(WorkQueueService workQueueService, DownloadJob downloadJob, UploadJob uploadJob) {
        this.workQueueService = workQueueService;
        this.downloadJob = downloadJob;
        this.uploadJob = uploadJob;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void clean() {
        List<QueueItem> queueItems = workQueueService.deleteCompleted();
        String queueName = workQueueService.getQueueDao().getQueueName();
        Set<Integer> queueItemsIds = queueItems.stream().map(QueueItem::getId).collect(Collectors.toSet());
        downloadJob.cleanUpDownloads(queueName, queueItemsIds);
        uploadJob.cleanUpUploads(queueName, queueItemsIds);

        LOGGER.debug("Delete completed({}, {})", queueItems.size(), new Date());
    }
}
