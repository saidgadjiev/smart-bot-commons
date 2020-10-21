package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.*;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.TelegramMediaServiceProvider;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMTProtoService;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Service
public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private TelegramMTProtoService telegramService;

    private TelegramMediaServiceProvider mediaServiceProvider;

    private FileLimitsDao fileLimitsDao;

    private LocalisationService localisationService;

    private UserService userService;

    @Value("${file.time.limit:180}")
    private int fileTimeLimit;

    @Autowired
    public FileManager(TelegramMTProtoService telegramService, TelegramMediaServiceProvider mediaServiceProvider,
                       FileLimitsDao fileLimitsDao, LocalisationService localisationService, UserService userService) {
        this.telegramService = telegramService;
        this.mediaServiceProvider = mediaServiceProvider;
        this.fileLimitsDao = fileLimitsDao;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("File time limit({})", fileTimeLimit);
    }

    public void setInputFilePending(long chatId, Integer replyToMessageId, String fileId, long fileSize, String command) {
        if (mediaServiceProvider.isBotApiDownloadFile(fileSize)) {
            return;
        }
        if (fileTimeLimit <= 0) {
            return;
        }
        fileLimitsDao.setInputFile(chatId, new InputFileState(replyToMessageId, fileId, command));
    }

    public void resetLimits(long chatId) {
        fileLimitsDao.deleteInputFile(chatId);
    }

    public void inputFile(long chatId, String fileId, long fileSize) {
        if (fileSize == 0) {
            LOGGER.debug("File size ({}, {}, {})", chatId, fileId, fileId);
        }
        if (mediaServiceProvider.isBotApiDownloadFile(fileSize)) {
            return;
        }
        InputFileState inputFileState = fileLimitsDao.getInputFile(chatId);
        if (inputFileState != null && fileTimeLimit > 0) {
            Long ttl = fileLimitsDao.getInputFileTtl(chatId);

            if (ttl == null || ttl == -1) {
                Integer replyToMessageId = inputFileState.getReplyToMessageId();
                Locale locale = userService.getLocaleOrDefault((int) chatId);
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INPUT_FILE_WAIT, locale)).setReplyToMessageId(replyToMessageId);
            } else {
                Locale locale = userService.getLocaleOrDefault((int) chatId);
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INPUT_FILE_WAIT_TTL, new Object[]{ttl}, locale));
            }
        }
    }

    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void forceDownloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        forceDownloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        mediaServiceProvider.getDownloadMediaService(fileSize).downloadFileByFileId(fileId, fileSize, progress, outputFile);
    }

    public void forceDownloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        boolean downloaded = false;
        Throwable lastEx = null;
        int floodWaitAttempts = 0;
        int unknownExceptionAttempts = 0;
        while (!downloaded && floodWaitAttempts < FileLimitProperties.FLOOD_WAIT_MAX_ATTEMPTS
                && unknownExceptionAttempts < FileLimitProperties.UNKNOWN_EXCEPTION_MAX_ATTEMPTS) {
            try {
                downloadFileByFileId(fileId, fileSize, progress, outputFile);
                downloaded = true;
            } catch (Throwable ex) {
                LOGGER.debug("Attemp({}, {}, {})", floodWaitAttempts, unknownExceptionAttempts, ex.getMessage());
                lastEx = ex;
                int unknownExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, UnknownDownloadingUploadingException.class);
                int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);
                if (unknownExceptionIndexOf == -1) {
                    unknownExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TimeoutException.class);
                }
                if (floodWaitExceptionIndexOf != -1) {
                    ++floodWaitAttempts;
                } else if (unknownExceptionIndexOf != -1) {
                    ++unknownExceptionAttempts;
                } else {
                    throw ex;
                }
                try {
                    Thread.sleep(FileLimitProperties.FLOOD_WAIT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    throw new DownloadingException(e);
                }
            }
        }

        if (!downloaded) {
            throw new DownloadingException(lastEx);
        }
    }

    public FileWorkObject fileWorkObject(long chatId, long fileSize) {
        return new FileWorkObject(fileTimeLimit, chatId, fileSize, fileLimitsDao, mediaServiceProvider);
    }

    public boolean cancelDownloading(String fileId) {
        return telegramService.cancelDownloading(fileId);
    }

    public boolean cancelUploading(String filePath) {
        return telegramService.cancelUploading(filePath);
    }

    public void cancelDownloads() {
        telegramService.cancelDownloads();
    }

    public void restoreFileIfNeed(String filePath, String fileId) {
        telegramService.restoreFileIfNeed(filePath, fileId);
    }

    public static boolean isNoneCriticalDownloadingException(Throwable ex) {
        int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);
        int timeoutException = ExceptionUtils.indexOfThrowable(ex, TimeoutException.class);

        return floodWaitExceptionIndexOf != -1 || timeoutException != -1;
    }
}
