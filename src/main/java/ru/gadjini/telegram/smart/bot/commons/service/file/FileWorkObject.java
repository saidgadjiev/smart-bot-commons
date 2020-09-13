package ru.gadjini.telegram.smart.bot.commons.service.file;

import ru.gadjini.telegram.smart.bot.commons.service.message.TelegramMediaServiceProvider;

import java.util.concurrent.TimeUnit;

public class FileWorkObject {

    private final int fileTimeLimit;

    private long chatId;

    private long fileSize;

    private FileLimitsDao fileLimitsDao;

    private TelegramMediaServiceProvider mediaServiceProvider;

    public FileWorkObject(int fileTimeLimit, long chatId, long fileSize, FileLimitsDao fileLimitsDao,
                          TelegramMediaServiceProvider mediaServiceProvider) {
        this.chatId = chatId;
        this.fileSize = fileSize;
        this.fileLimitsDao = fileLimitsDao;
        this.mediaServiceProvider = mediaServiceProvider;
        this.fileTimeLimit = fileTimeLimit;
    }

    public long getChatId() {
        return chatId;
    }

    public void start() {
        if (mediaServiceProvider.isBotApiDownloadFile(fileSize)) {
            return;
        }
        if (fileTimeLimit <= 0) {
            return;
        }
        if (fileLimitsDao.hasInputFile(chatId)) {
            fileLimitsDao.setState(chatId, InputFileState.State.PROCESSING);
        }
    }

    public void stop() {
        if (mediaServiceProvider.isBotApiDownloadFile(fileSize)) {
            return;
        }
        if (fileTimeLimit <= 0) {
            return;
        }
        if (fileLimitsDao.hasInputFile(chatId)) {
            fileLimitsDao.setState(chatId, InputFileState.State.COMPLETED);
            fileLimitsDao.setInputFileTtl(chatId, fileTimeLimit, TimeUnit.SECONDS);
        }
    }
}
