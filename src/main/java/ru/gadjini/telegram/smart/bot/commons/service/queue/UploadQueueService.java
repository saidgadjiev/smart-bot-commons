package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.UploadQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;

import java.util.List;
import java.util.Set;

@Service
public class UploadQueueService extends QueueService {

    private UploadQueueDao uploadQueueDao;

    @Autowired
    public UploadQueueService(UploadQueueDao uploadQueueDao) {
        this.uploadQueueDao = uploadQueueDao;
    }

    public void createUpload(int userId, String method, Object body, Progress progress, String producer,
                             int producerId, Object extra) {
        UploadQueueItem uploadQueueItem = new UploadQueueItem();
        uploadQueueItem.setUserId(userId);
        uploadQueueItem.setMethod(method);
        uploadQueueItem.setBody(body);
        uploadQueueItem.setProducer(producer);
        uploadQueueItem.setProgress(progress);
        uploadQueueItem.setProducerId(producerId);
        uploadQueueItem.setStatus(QueueItem.Status.WAITING);
        uploadQueueItem.setExtra(extra);

        uploadQueueDao.create(uploadQueueItem);
    }

    public void createUpload(int userId, String method, Object body, Progress progress, String producer, int producerId) {
        createUpload(userId, method, body, progress, producer, producerId, null);
    }

    public List<UploadQueueItem> poll(String producer, int limit) {
        return uploadQueueDao.poll(producer, limit);
    }

    public List<UploadQueueItem> getUploads(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.getUploads(producer, producerIds);
    }

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    @Override
    public QueueDao getQueueDao() {
        return uploadQueueDao;
    }
}
