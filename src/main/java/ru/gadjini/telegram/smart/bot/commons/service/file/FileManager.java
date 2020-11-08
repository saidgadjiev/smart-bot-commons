package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.FloodWaitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Objects;

@Service
public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private TelegramBotApiService telegramLocalBotApiService;

    @Autowired
    public FileManager(TelegramBotApiService telegramLocalBotApiService) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        boolean downloaded = false;
        Throwable lastEx = null;
        int floodWaitExceptionAttempts = 1;
        int sleepTime = FloodWaitProperties.SLEEP_TIME_BEFORE_ATTEMPT;
        while (!downloaded && floodWaitExceptionAttempts <= FloodWaitProperties.SLEEP_TIME_BEFORE_ATTEMPT) {
            try {
                telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
                downloaded = true;
            } catch (Throwable ex) {
                lastEx = ex;
                int telegramApiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);
                if (telegramApiRequestExceptionIndexOf != -1) {
                    TelegramApiRequestException apiRequestException = (TelegramApiRequestException) ExceptionUtils.getThrowables(ex)[telegramApiRequestExceptionIndexOf];
                    if (Objects.equals(apiRequestException.getApiResponse(), "Bad Request: wrong file_id or the file is temporarily unavailable")) {
                        LOGGER.debug("Attemp({}, {}, {})", floodWaitExceptionAttempts, ex.getMessage(), sleepTime);
                        ++floodWaitExceptionAttempts;
                    } else {
                        throw ex;
                    }
                } else {
                    throw ex;
                }
                try {
                    Thread.sleep(sleepTime);
                    sleepTime += FloodWaitProperties.SLEEP_TIME_BEFORE_ATTEMPT;
                } catch (InterruptedException e) {
                    throw new DownloadingException(e);
                }
            }
        }

        if (!downloaded) {
            throw new DownloadingException(lastEx);
        }
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
