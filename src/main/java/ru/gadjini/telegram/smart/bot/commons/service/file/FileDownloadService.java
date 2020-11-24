package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadingJob;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadingQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class FileDownloadService {

    private TelegramBotApiService telegramLocalBotApiService;

    private DownloadingQueueService queueService;

    private WorkQueueDao workQueueDao;

    private DownloadingJob downloadingJob;

    @Autowired
    public FileDownloadService(TelegramBotApiService telegramLocalBotApiService,
                               DownloadingQueueService queueService, WorkQueueDao workQueueDao) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.queueService = queueService;
        this.workQueueDao = workQueueDao;
    }

    @Autowired
    public void setDownloadingJob(DownloadingJob downloadingJob) {
        this.downloadingJob = downloadingJob;
    }

    public void createDownload(TgFile file, int producerId, int userId) {
        createDownloads(Collections.singletonList(file), producerId, userId);
    }

    public void createDownloads(Collection<TgFile> files, int producerId, int userId) {
        queueService.create(files, workQueueDao.getQueueName(), producerId, userId);
    }

    public List<DownloadingQueueItem> getDownloadsIfReadyElseNull(int producerId) {
        List<DownloadingQueueItem> downloads = queueService.getDownloads(workQueueDao.getQueueName(), producerId);

        return downloads.stream().allMatch(downloadingQueueItem -> downloadingQueueItem.getStatus().equals(QueueItem.Status.COMPLETED)) ? downloads : null;
    }

    public void cancelDownloads(int producerId) {
        downloadingJob.cancelDownloads(workQueueDao.getQueueName(), producerId);
    }

    public void cancelDownloads(Set<Integer> producerIds) {
        downloadingJob.cancelDownloads(workQueueDao.getQueueName(), producerIds);
    }

    public boolean cancelUpload(String filePath) {
        return telegramLocalBotApiService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        downloadingJob.cancelDownloads();
    }

    public void deleteDownloads(int producerId) {
        queueService.deleteByProducer(workQueueDao.getQueueName(), producerId);
    }
}
