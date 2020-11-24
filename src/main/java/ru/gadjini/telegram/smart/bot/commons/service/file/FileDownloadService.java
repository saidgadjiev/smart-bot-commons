package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.service.flood.FloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadingQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class FileDownloadService {

    private TelegramBotApiService telegramLocalBotApiService;

    private FloodWaitController floodWaitController;

    private DownloadingQueueService queueService;

    private WorkQueueDao workQueueDao;

    @Autowired
    public FileDownloadService(TelegramBotApiService telegramLocalBotApiService, FloodWaitController floodWaitController,
                               DownloadingQueueService queueService, WorkQueueDao workQueueDao) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.floodWaitController = floodWaitController;
        this.queueService = queueService;
        this.workQueueDao = workQueueDao;
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

    public boolean cancelDownload(String fileId, long fileSize, int producerId) {
        floodWaitController.cancelDownloading(fileId, fileSize);
        queueService.deleteByFileId(fileId, workQueueDao.getQueueName(), producerId);

        return telegramLocalBotApiService.cancelDownloading(fileId);
    }

    public boolean cancelUpload(String filePath) {
        return telegramLocalBotApiService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        telegramLocalBotApiService.cancelDownloads();
    }

    public void deleteDownloads(int producerId) {
        queueService.deleteByProducer(workQueueDao.getQueueName(), producerId);
    }
}
