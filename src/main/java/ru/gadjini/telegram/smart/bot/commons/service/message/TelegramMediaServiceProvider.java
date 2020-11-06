package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.InputFile;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.InputMedia;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramLocalBotApiService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMediaService;

import static ru.gadjini.telegram.smart.bot.commons.common.TgConstants.BOT_API_DOWNLOAD_FILE_LIMIT;
import static ru.gadjini.telegram.smart.bot.commons.common.TgConstants.BOT_API_UPLOAD_FILE_LIMIT;

@Component
@SuppressWarnings("PMD")
public class TelegramMediaServiceProvider {

    private TelegramLocalBotApiService localBotApiService;

    @Autowired
    public TelegramMediaServiceProvider(TelegramLocalBotApiService localBotApiService) {
        this.localBotApiService = localBotApiService;
    }

    public boolean isBotApiDownloadFile(long fileSize) {
        return fileSize > 0 && fileSize <= BOT_API_DOWNLOAD_FILE_LIMIT;
    }

    public boolean isBotApiUploadFile(long fileSize) {
        return fileSize > 0 && fileSize <= BOT_API_UPLOAD_FILE_LIMIT;
    }

    public TelegramMediaService getMediaService(InputMedia media) {
        return getMediaService(media.getFileId(), media.getFilePath());
    }

    public TelegramMediaService getMediaService(InputFile media) {
        return getMediaService(media.getFileId(), media.getFilePath());
    }

    public TelegramMediaService getStickerMediaService() {
        return localBotApiService;
    }

    public TelegramMediaService getMediaService(String fileId, String filePath) {
        return localBotApiService;
    }

    public TelegramMediaService getDownloadMediaService(long fileSize) {
        return localBotApiService;
    }
}
