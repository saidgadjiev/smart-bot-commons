package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

@Service
public class FileManager {

    private TelegramBotApiService telegramLocalBotApiService;

    @Autowired
    public FileManager(TelegramBotApiService telegramLocalBotApiService) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
    }

    public boolean cancelDownloading(String fileId) {
        return telegramLocalBotApiService.cancelDownloading(fileId);
    }

    public boolean cancelUploading(String filePath) {
        return telegramLocalBotApiService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        telegramLocalBotApiService.cancelDownloads();
    }

    public static boolean isNoneCriticalDownloadingException(Throwable ex) {
        int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);

        return floodWaitExceptionIndexOf != -1;
    }

    public static boolean isNoneCriticalDownloadingException(String exception) {
        if (StringUtils.isBlank(exception)) {
            return false;
        }
        return exception.contains(FloodWaitException.class.getSimpleName());
    }
}
