package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;

public interface MediaMessageService {
    EditMediaResult editMessageMedia(EditMessageMedia editMediaContext);

    SendFileResult sendSticker(SendSticker sendSticker);

    SendFileResult sendDocument(SendDocument sendDocumentContext, Progress progress);

    void sendFile(long chatId, String fileId);

    default SendFileResult sendDocument(SendDocument sendDocumentContext) {
        return sendDocument(sendDocumentContext, null);
    }

    SendFileResult sendPhoto(SendPhoto sendPhoto);

    void sendVideo(SendVideo sendVideo);

    SendFileResult sendAudio(SendAudio sendAudio);

}
