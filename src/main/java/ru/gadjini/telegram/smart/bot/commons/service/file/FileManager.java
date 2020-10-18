package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.TelegramMediaServiceProvider;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMTProtoService;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;

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
        int attempts = 0;
        while (!downloaded && attempts < FileLimitProperties.MAX_ATTEMPTS) {
            ++attempts;
            try {
                downloadFileByFileId(fileId, fileSize, progress, outputFile);
                downloaded = true;
            } catch (Throwable ex) {
                LOGGER.debug("Attemp({}, {})", attempts, ex.getMessage());
                lastEx = ex;
                int downloadingExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, DownloadingException.class);
                int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);
                if (downloadingExceptionIndexOf == -1 && floodWaitExceptionIndexOf == -1) {
                    throw ex;
                } else {
                    try {
                        Thread.sleep(FileLimitProperties.FLOOD_WAIT_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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
}
