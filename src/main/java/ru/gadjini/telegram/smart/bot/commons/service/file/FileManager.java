package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
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
import ru.gadjini.telegram.smart.bot.commons.utils.ThreadUtils;

import java.net.SocketException;
import java.util.Objects;

@Service
public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private TelegramBotApiService telegramLocalBotApiService;

    private static final String FILE_ID_TEMPORARILY_UNAVAILABLE = "Bad Request: wrong file_id or the file is temporarily unavailable";

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
        int attempts = 1;
        int sleepTime = FloodWaitProperties.SLEEP_TIME_BEFORE_ATTEMPT;
        while (!downloaded && attempts <= FloodWaitProperties.FLOOD_WAIT_MAX_ATTEMPTS) {
            try {
                telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
                downloaded = true;
            } catch (Throwable ex) {
                lastEx = ex;
                if (shouldTryToDownloadAgain(ex)) {
                    LOGGER.debug("Attemp({}, {}, {})", attempts, ex.getMessage(), sleepTime);
                    ThreadUtils.sleep(sleepTime, DownloadingException::new);
                    ++attempts;
                    sleepTime += FloodWaitProperties.SLEEP_TIME_BEFORE_ATTEMPT;
                } else {
                    throw ex;
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
        return shouldTryToDownloadAgain(ex);
    }

    public static boolean isNoneCriticalDownloadingException(String exception) {
        if (StringUtils.isBlank(exception)) {
            return false;
        }
        return exception.contains(FloodWaitException.class.getSimpleName()) ||
                exception.contains(NoHttpResponseException.class.getSimpleName()) ||
                exception.contains(SocketException.class.getSimpleName()) ||
                exception.contains(FILE_ID_TEMPORARILY_UNAVAILABLE);
    }

    private static boolean shouldTryToDownloadAgain(Throwable ex) {
        int telegramApiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);
        int indexOfNoResponseException = ExceptionUtils.indexOfThrowable(ex, NoHttpResponseException.class);
        int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);
        if (telegramApiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException apiRequestException = (TelegramApiRequestException) ExceptionUtils.getThrowables(ex)[telegramApiRequestExceptionIndexOf];

            return Objects.equals(apiRequestException.getApiResponse(), FILE_ID_TEMPORARILY_UNAVAILABLE);
        } else {
            return indexOfNoResponseException != -1 || socketException != -1;
        }
    }

}
