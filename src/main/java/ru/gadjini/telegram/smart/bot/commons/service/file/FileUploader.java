package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

@Service
public class FileUploader {

    private TelegramBotApiService telegramBotApiService;

    @Autowired
    public FileUploader(TelegramBotApiService telegramBotApiService) {
        this.telegramBotApiService = telegramBotApiService;
    }

    public boolean cancelUploading(String filePath) {
        return telegramBotApiService.cancelUploading(filePath);
    }
}
