package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadJob;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Service
public class FileDownloadService {

    private DownloadQueueService queueService;

    private WorkQueueDao workQueueDao;

    private DownloadJob downloadingJob;

    @Autowired
    public FileDownloadService(DownloadQueueService queueService, WorkQueueDao workQueueDao) {
        this.queueService = queueService;
        this.workQueueDao = workQueueDao;
    }

    @Autowired
    public void setDownloadingJob(DownloadJob downloadingJob) {
        this.downloadingJob = downloadingJob;
    }

    public void createDownload(TgFile file, int producerId, int userId) {
        createDownloads(Collections.singletonList(file), producerId, userId);
    }

    public void createDownloads(Collection<TgFile> files, int producerId, int userId) {
        queueService.create(files, workQueueDao.getQueueName(), producerId, userId);
    }

    public void cancelDownloads(int producerId) {
        downloadingJob.cancelDownloads(workQueueDao.getQueueName(), producerId);
    }

    public void cancelDownloads(Set<Integer> producerIds) {
        downloadingJob.cancelDownloads(workQueueDao.getQueueName(), producerIds);
    }

    public void cancelDownloads() {
        downloadingJob.cancelDownloads();
    }
}
