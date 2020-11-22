package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.service.DownloadingQueueService;
import ru.gadjini.telegram.smart.bot.commons.service.flood.FloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class FileDownloadService {

    private TelegramBotApiService telegramLocalBotApiService;

    private FloodWaitController floodWaitController;

    private DownloadingQueueService queueService;

    private QueueDao queueDao;

    @Autowired
    public FileDownloadService(TelegramBotApiService telegramLocalBotApiService, FloodWaitController floodWaitController,
                               DownloadingQueueService queueService, QueueDao queueDao) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.floodWaitController = floodWaitController;
        this.queueService = queueService;
        this.queueDao = queueDao;
    }

    public void createDownload(TgFile file) {
        createDownloads(Collections.singletonList(file));
    }

    public void createDownloads(Collection<TgFile> files) {
        queueService.create(files, queueDao.getQueueName());
    }

    public List<DownloadingQueueItem> getDownloadsIfReadyElseNull(Collection<String> filesIds) {
        List<DownloadingQueueItem> downloads = queueService.getDownloads(filesIds);

        return downloads.stream().allMatch(downloadingQueueItem -> downloadingQueueItem.getStatus().equals(QueueItem.Status.COMPLETED)) ? downloads : null;
    }

    public boolean cancelDownload(String fileId, long fileSize) {
        floodWaitController.cancelDownloading(fileId, fileSize);
        queueService.deleteByFileId(fileId);

        return telegramLocalBotApiService.cancelDownloading(fileId);
    }

    public boolean cancelUpload(String filePath) {
        return telegramLocalBotApiService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        telegramLocalBotApiService.cancelDownloads();
    }

    public void deleteDownloads(Collection<Integer> ids) {
        queueService.deleteByIds(ids);
    }
}
