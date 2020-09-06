package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.message.TelegramMediaServiceProvider;

@Service
public class ProgressManager {

    private TelegramMediaServiceProvider mediaServiceProvider;

    @Autowired
    public ProgressManager(TelegramMediaServiceProvider mediaServiceProvider) {
        this.mediaServiceProvider = mediaServiceProvider;
    }

    public boolean isShowingDownloadingProgress(long fileSize) {
        return !mediaServiceProvider.isBotApiDownloadFile(fileSize);
    }
}
