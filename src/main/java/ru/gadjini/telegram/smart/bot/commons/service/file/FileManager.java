package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
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
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Service
public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private TelegramBotApiService telegramLocalBotApiService;

    private FileLimitsDao fileLimitsDao;

    private LocalisationService localisationService;

    private UserService userService;

    @Value("${file.time.limit:180}")
    private int fileTimeLimit;

    @Value("${max.file.size:#{null}}")
    private Long maxFileSize;

    @Autowired
    public FileManager(TelegramBotApiService telegramLocalBotApiService,
                       FileLimitsDao fileLimitsDao, LocalisationService localisationService, UserService userService) {
        this.telegramLocalBotApiService = telegramLocalBotApiService;
        this.fileLimitsDao = fileLimitsDao;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("File time limit({})", fileTimeLimit);

        if (maxFileSize != null) {
            LOGGER.debug("Max file size({})", MemoryUtils.humanReadableByteCount(maxFileSize));
        }
    }

    public void setInputFilePending(long chatId, Integer replyToMessageId, String fileId, String command) {
        if (fileTimeLimit <= 0) {
            return;
        }
        fileLimitsDao.setInputFile(chatId, new InputFileState(replyToMessageId, fileId, command));
    }

    public void resetLimits(long chatId) {
        fileLimitsDao.deleteInputFile(chatId);
    }

    public void inputFile(long chatId, String fileId, long fileSize) {
        checkFileSize(fileSize, chatId);
        if (fileSize == 0) {
            LOGGER.debug("File size ({}, {}, {})", chatId, fileId, fileId);
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
        telegramLocalBotApiService.downloadFileByFileId(fileId, fileSize, progress, outputFile);
    }

    public void forceDownloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        boolean downloaded = false;
        Throwable lastEx = null;
        int unknownExceptionAttempts = 0;
        while (!downloaded && unknownExceptionAttempts < FileLimitProperties.UNKNOWN_EXCEPTION_MAX_ATTEMPTS) {
            try {
                downloadFileByFileId(fileId, fileSize, progress, outputFile);
                downloaded = true;
            } catch (Throwable ex) {
                lastEx = ex;
                int unknownExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, UnknownDownloadingUploadingException.class);
                if (unknownExceptionIndexOf == -1) {
                    unknownExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TimeoutException.class);
                }
                if (unknownExceptionIndexOf != -1) {
                    LOGGER.debug("Attemp({}, {})", unknownExceptionAttempts, ex.getMessage());
                    ++unknownExceptionAttempts;
                } else {
                    throw ex;
                }
                try {
                    Thread.sleep(FileLimitProperties.SLEEP_TIME_BEFORE_ATTEMPT);
                } catch (InterruptedException e) {
                    throw new DownloadingException(e);
                }
            }
        }

        if (!downloaded) {
            throw new DownloadingException(lastEx);
        }
    }

    public FileWorkObject fileWorkObject(long chatId) {
        return new FileWorkObject(fileTimeLimit, chatId, fileLimitsDao);
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
        int timeoutException = ExceptionUtils.indexOfThrowable(ex, TimeoutException.class);

        return floodWaitExceptionIndexOf != -1 || timeoutException != -1;
    }

    public static boolean isNoneCriticalDownloadingException(String exception) {
        if (StringUtils.isBlank(exception)) {
            return false;
        }
        boolean floodWait = exception.contains(FloodWaitException.class.getSimpleName());
        boolean timeout = exception.contains(TimeoutException.class.getSimpleName());

        return floodWait || timeout;
    }

    private void checkFileSize(long fileSize, long chatId) {
        if (maxFileSize != null && fileSize > maxFileSize) {
            Locale locale = userService.getLocaleOrDefault((int) chatId);
            throw new UserException(localisationService.getMessage(MessagesProperties.MAX_FILE_SIZE, new Object[]{
                    MemoryUtils.humanReadableByteCount(maxFileSize)
            }, locale));
        }
    }
}
