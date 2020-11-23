package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gadjini.telegram.smart.bot.commons.dao.DownloadingQueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;

import java.util.Collection;
import java.util.List;

@Service
public class DownloadingQueueService extends QueueService {

    private DownloadingQueueDao downloadingQueueDao;

    @Autowired
    public DownloadingQueueService(DownloadingQueueDao downloadingQueueDao) {
        this.downloadingQueueDao = downloadingQueueDao;
    }

    public List<DownloadingQueueItem> poll(String producer) {
        return downloadingQueueDao.poll(producer);
    }

    @Transactional
    public void create(Collection<TgFile> files, String producer, int producerId) {
        for (TgFile file : files) {
            DownloadingQueueItem queueItem = new DownloadingQueueItem();
            queueItem.setFile(file);
            queueItem.setProducer(producer);
            queueItem.setProgress(file.getProgress());
            queueItem.setFilePath(file.getFilePath());
            queueItem.setDeleteParentDir(file.isDeleteParentDir());
            queueItem.setProducerId(producerId);

            downloadingQueueDao.create(queueItem);
        }
    }

    public List<DownloadingQueueItem> getDownloads(String producer, int producerId) {
        return downloadingQueueDao.getDownloads(producer, producerId);
    }

    public void setCompleted(int id, String filePath) {
        downloadingQueueDao.setCompleted(id, filePath);
    }

    public void deleteByFileId(String fileId, String producer, int producerId) {
        downloadingQueueDao.deleteByFileId(fileId, producer, producerId);
    }

    public void deleteByProducer(String producer, int producerId) {
        downloadingQueueDao.deleteByProducerId(producer, producerId);
    }

    @Override
    public QueueDao getQueueDao() {
        return downloadingQueueDao;
    }
}
