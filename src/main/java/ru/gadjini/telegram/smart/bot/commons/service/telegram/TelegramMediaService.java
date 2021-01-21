package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;

public interface TelegramMediaService {
    Message editMessageMedia(EditMessageMedia editMessageMedia);

    Message sendSticker(SendSticker sendSticker, Progress progress);

    Message sendDocument(SendDocument sendDocument, Progress progress);

    Message sendVideo(SendVideo sendVideo, Progress progress);

    Message sendAudio(SendAudio sendAudio, Progress progress);

    Message sendVoice(SendVoice sendVoice, Progress progress);

    Message sendPhoto(SendPhoto sendPhoto);

    void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile);

    void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile);

    void cancelUploading(String filePath);

    void cancelDownloading(String fileId);

    void cancelDownloads();
}
