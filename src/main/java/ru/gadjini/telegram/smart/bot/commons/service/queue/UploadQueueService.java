package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.UploadQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;

import java.util.List;
import java.util.Set;

@Service
public class UploadQueueService extends QueueService {

    private UploadQueueDao uploadQueueDao;

    private FileUploader fileUploader;

    @Autowired
    public UploadQueueService(UploadQueueDao uploadQueueDao) {
        this.uploadQueueDao = uploadQueueDao;
    }

    @Autowired
    public void setFileUploader(FileUploader fileUploader) {
        this.fileUploader = fileUploader;
    }

    public UploadQueueItem createUpload(int userId, String method, Object body, Progress progress, String producerTable, String producer,
                             int producerId, QueueItem.Status status, Object extra) {
        UploadQueueItem uploadQueueItem = new UploadQueueItem();
        uploadQueueItem.setUserId(userId);
        uploadQueueItem.setMethod(method);
        uploadQueueItem.setBody(body);
        uploadQueueItem.setProducerTable(producerTable);
        uploadQueueItem.setProducer(producer);
        uploadQueueItem.setProgress(progress);
        uploadQueueItem.setProducerId(producerId);
        uploadQueueItem.setStatus(status);
        uploadQueueItem.setExtra(extra);
        uploadQueueItem.setFileSize(fileUploader.getInputFile(method, body).getNewMediaFile().length());

        uploadQueueDao.create(uploadQueueItem);

        return uploadQueueItem;
    }

    public List<UploadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return uploadQueueDao.poll(producer, jobWeight, limit);
    }

    public void updateStatus(int id, QueueItem.Status newStatus, QueueItem.Status oldStatus) {
        uploadQueueDao.updateStatus(id, newStatus, oldStatus);
    }

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    public List<UploadQueueItem> deleteOrphanUploads(String producer) {
        return uploadQueueDao.deleteOrphan(producer);
    }

    @Override
    public QueueDao getQueueDao() {
        return uploadQueueDao;
    }
}
