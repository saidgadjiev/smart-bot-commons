package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.UploadQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileUploader;

import java.util.ArrayList;
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

    public void createUpload(int userId, String method, Object body, Progress progress, String producerTable, String producer,
                             int producerId, Object extra) {
        UploadQueueItem uploadQueueItem = new UploadQueueItem();
        uploadQueueItem.setUserId(userId);
        uploadQueueItem.setMethod(method);
        uploadQueueItem.setBody(body);
        uploadQueueItem.setProducerTable(producerTable);
        uploadQueueItem.setProducer(producer);
        uploadQueueItem.setProgress(progress);
        uploadQueueItem.setProducerId(producerId);
        uploadQueueItem.setStatus(QueueItem.Status.WAITING);
        uploadQueueItem.setExtra(extra);
        uploadQueueItem.setFileSize(fileUploader.getInputFile(method, body).getNewMediaFile().length());

        uploadQueueDao.create(uploadQueueItem);
    }

    public List<UploadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return uploadQueueDao.poll(producer, jobWeight, limit);
    }

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        return uploadQueueDao.deleteByProducerIdsWithReturning(producer, producerIds);
    }

    public List<UploadQueueItem> deleteOrphanUploads(String producer, String producerTable) {
        return uploadQueueDao.deleteOrphan(producer, producerTable);
    }

    public void deleteCompletedAndOrphans(String producer, String producerTable, Set<Integer> producerIds) {
        List<UploadQueueItem> deleted = new ArrayList<>(deleteByProducerIdsWithReturning(producer, producerIds));
        List<UploadQueueItem> orphanUploads = deleteOrphanUploads(producer, producerTable);
        deleted.addAll(orphanUploads);
        releaseResources(deleted);
    }

    public void releaseResources(List<UploadQueueItem> uploadQueueItems) {
        for (UploadQueueItem uploadQueueItem : uploadQueueItems) {
            releaseResources(uploadQueueItem);
        }
    }

    public void releaseResources(UploadQueueItem uploadQueueItem) {
        if (uploadQueueItem == null) {
            return;
        }
        InputFile inputFile = null;
        InputFile thumb = null;
        switch (uploadQueueItem.getMethod()) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) uploadQueueItem.getBody();
                inputFile = sendDocument.getDocument();
                thumb = sendDocument.getThumb();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) uploadQueueItem.getBody();
                inputFile = sendAudio.getAudio();
                thumb = sendAudio.getThumb();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) uploadQueueItem.getBody();
                inputFile = sendVideo.getVideo();
                thumb = sendVideo.getThumb();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) uploadQueueItem.getBody();
                inputFile = sendVoice.getVoice();
                break;
            }
            case SendSticker.PATH: {
                SendSticker sendSticker = (SendSticker) uploadQueueItem.getBody();
                inputFile = sendSticker.getSticker();
                break;
            }
        }

        if (inputFile != null && inputFile.isNew()) {
            new SmartTempFile(inputFile.getNewMediaFile()).smartDelete();
        }
        if (thumb != null && thumb.isNew()) {
            new SmartTempFile(thumb.getNewMediaFile()).smartDelete();
        }
    }

    @Override
    public QueueDao getQueueDao() {
        return uploadQueueDao;
    }
}
