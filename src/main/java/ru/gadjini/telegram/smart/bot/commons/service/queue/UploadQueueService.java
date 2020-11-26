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

    public void createUpload(int userId, String method, Object body, Progress progress, String producer, int producerId) {
        UploadQueueItem uploadQueueItem = new UploadQueueItem();
        uploadQueueItem.setUserId(userId);
        uploadQueueItem.setMethod(method);
        uploadQueueItem.setBody(body);
        uploadQueueItem.setProducer(producer);
        uploadQueueItem.setProgress(progress);
        uploadQueueItem.setProducerId(producerId);
        uploadQueueItem.setStatus(QueueItem.Status.WAITING);

        uploadQueueDao.create(uploadQueueItem);
    }

    public List<UploadQueueItem> poll(String producer, int limit) {
        return uploadQueueDao.poll(producer, limit);
    }

    public List<UploadQueueItem> getUploads(String producer, int producerId) {
        return uploadQueueDao.getUploads(producer, Set.of(producerId));
    }

    public List<UploadQueueItem> getUploads(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.getUploads(producer, producerIds);
    }

    public void deleteByProducer(String producer, int producerId) {
        uploadQueueDao.deleteByProducerIds(producer, Set.of(producerId));
    }

    public void deleteByProducer(String producer, Set<Integer> producerIds) {
        uploadQueueDao.deleteByProducerIds(producer, producerIds);
    }

    @Override
    public QueueDao getQueueDao() {
        return uploadQueueDao;
    }
}
