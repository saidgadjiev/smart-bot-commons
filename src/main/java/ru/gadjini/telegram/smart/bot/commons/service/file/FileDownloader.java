package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.flood.FloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.net.SocketException;
import java.util.Objects;

@Service
public class FileDownloader {

    private static final String FILE_ID_TEMPORARILY_UNAVAILABLE = "Bad Request: wrong file_id or the file is temporarily unavailable";

    private TelegramBotApiService telegramLocalBotApiService;

    private FloodWaitController floodWaitController;

    @Autowired
    public FileDownloader(TelegramBotApiService telegramLocalBotApiService, FloodWaitController floodWaitController) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.floodWaitController = floodWaitController;
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        floodWaitController.startDownloading(fileId);
        try {
            try {
                telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
            } catch (ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException ex) {
                if (isWrongFileIdException(ex)) {
                    floodWaitController.downloadingFloodWait(fileSize);
                } else {
                    throw ex;
                }
            }
        } finally {
            floodWaitController.finishDownloading(fileId, fileSize);
        }
    }

    public boolean cancelDownloading(String fileId, long fileSize) {
        floodWaitController.cancelDownloading(fileId, fileSize);

        return telegramLocalBotApiService.cancelDownloading(fileId);
    }

    public boolean cancelUploading(String filePath) {
        return telegramLocalBotApiService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        telegramLocalBotApiService.cancelDownloads();
    }

    public static boolean isNoneCriticalDownloadingException(Throwable ex) {
        int indexOfNoResponseException = ExceptionUtils.indexOfThrowable(ex, NoHttpResponseException.class);
        int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);

        return indexOfNoResponseException != -1 || socketException != -1;
    }

    private boolean isWrongFileIdException(ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException ex) {
        int telegramApiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);

        if (telegramApiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException apiRequestException = (TelegramApiRequestException) ExceptionUtils.getThrowables(ex)[telegramApiRequestExceptionIndexOf];

            return Objects.equals(apiRequestException.getApiResponse(), FILE_ID_TEMPORARILY_UNAVAILABLE);
        }

        return false;
    }
}