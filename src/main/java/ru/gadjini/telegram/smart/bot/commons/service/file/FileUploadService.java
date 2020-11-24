package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import java.util.Set;

@Service
public class FileUploadService {

    private UploadQueueService uploadQueueService;

    private UploadJob uploadJob;

    private WorkQueueDao workQueueDao;

    @Autowired
    public FileUploadService(UploadQueueService uploadQueueService, UploadJob uploadJob, WorkQueueDao workQueueDao) {
        this.uploadQueueService = uploadQueueService;
        this.uploadJob = uploadJob;
        this.workQueueDao = workQueueDao;
    }

    public void createUpload(int userId, String method, Object body, Progress progress, int producerId) {
        uploadQueueService.createUpload(userId, method, body, progress, workQueueDao.getQueueName(), producerId);
    }

    public void cancelUploads(int producerId) {
        uploadJob.cancelUploads(workQueueDao.getQueueName(), producerId);
    }

    public void cancelUploads(Set<Integer> producerIds) {
        uploadJob.cancelUploads(workQueueDao.getQueueName(), producerIds);
    }

    public void cancelUploads() {
        uploadJob.cancelUploads();
    }
}
