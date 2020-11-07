package ru.gadjini.telegram.smart.bot.commons.service.file;

import java.util.concurrent.TimeUnit;

public class FileWorkObject {

    private final int fileTimeLimit;

    private long chatId;

    private FileLimitsDao fileLimitsDao;

    public FileWorkObject(int fileTimeLimit, long chatId, FileLimitsDao fileLimitsDao) {
        this.chatId = chatId;
        this.fileLimitsDao = fileLimitsDao;
        this.fileTimeLimit = fileTimeLimit;
    }

    public long getChatId() {
        return chatId;
    }

    public void start() {
        if (fileTimeLimit <= 0) {
            return;
        }
        if (fileLimitsDao.hasInputFile(chatId)) {
            fileLimitsDao.setState(chatId, InputFileState.State.PROCESSING);
        }
    }

    public void stop() {
        if (fileTimeLimit <= 0) {
            return;
        }
        if (fileLimitsDao.hasInputFile(chatId)) {
            fileLimitsDao.setState(chatId, InputFileState.State.COMPLETED);
            fileLimitsDao.setInputFileTtl(chatId, fileTimeLimit, TimeUnit.SECONDS);
        }
    }
}
