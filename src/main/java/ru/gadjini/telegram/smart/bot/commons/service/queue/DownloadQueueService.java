package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.telegram.smart.bot.commons.dao.DownloadingQueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class DownloadQueueService extends QueueService {

    private DownloadingQueueDao downloadingQueueDao;

    @Autowired
    public DownloadQueueService(DownloadingQueueDao downloadingQueueDao) {
        this.downloadingQueueDao = downloadingQueueDao;
    }

    public List<DownloadQueueItem> poll(String producer, SmartExecutorService.JobWeight weight, int limit) {
        return downloadingQueueDao.poll(producer, weight, limit);
    }

    @Transactional
    public void create(Collection<TgFile> files, String producer, int producerId, int userId) {
        for (TgFile file : files) {
            DownloadQueueItem queueItem = new DownloadQueueItem();
            queueItem.setFile(file);
            queueItem.setProducer(producer);
            queueItem.setProgress(file.getProgress());
            queueItem.setFilePath(file.getFilePath());
            queueItem.setDeleteParentDir(file.isDeleteParentDir());
            queueItem.setProducerId(producerId);
            queueItem.setStatus(QueueItem.Status.WAITING);
            queueItem.setUserId(userId);

            downloadingQueueDao.create(queueItem);
        }
    }

    public List<DownloadQueueItem> getDownloads(String producer, int producerId) {
        return getDownloads(producer, Set.of(producerId));
    }

    public List<DownloadQueueItem> getDownloads(String producer, Set<Integer> producerIds) {
        return downloadingQueueDao.getDownloads(producer, producerIds);
    }

    public void setCompleted(int id, String filePath) {
        downloadingQueueDao.setCompleted(id, filePath);
    }

    public List<DownloadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return downloadingQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    public long countWrongFileIdErrors() {
        return downloadingQueueDao.countWrongFileIdErrors();
    }

    @Override
    public QueueDao getQueueDao() {
        return downloadingQueueDao;
    }
}
