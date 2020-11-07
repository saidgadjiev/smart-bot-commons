package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.MediaType;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.*;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.ParseMode;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

@Service
@Qualifier("media")
public class MediaMessageServiceImpl implements MediaMessageService {

    private MessageMediaService fileService;

    private TelegramBotApiService telegramLocalBotApiService;

    @Autowired
    public MediaMessageServiceImpl(MessageMediaService fileService, TelegramBotApiService telegramLocalBotApiService) {
        this.fileService = fileService;
        this.telegramLocalBotApiService = telegramLocalBotApiService;
    }

    @Override
    public EditMediaResult editMessageMedia(EditMessageMedia editMessageMedia) {
        if (StringUtils.isNotBlank(editMessageMedia.getMedia().getCaption())) {
            editMessageMedia.getMedia().setParseMode(ParseMode.HTML);
        }
        Message message = telegramLocalBotApiService.editMessageMedia(editMessageMedia);

        return new EditMediaResult(fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument) {
        if (StringUtils.isNotBlank(sendDocument.getCaption())) {
            sendDocument.setParseMode(ParseMode.HTML);
        }

        Message message = telegramLocalBotApiService.sendDocument(sendDocument);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        Message message = telegramLocalBotApiService.sendPhoto(sendPhoto);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public void sendVideo(SendVideo sendVideo) {
        if (StringUtils.isNotBlank(sendVideo.getCaption())) {
            sendVideo.setParseMode(ParseMode.HTML);
        }
        telegramLocalBotApiService.sendVideo(sendVideo);
    }

    @Override
    public void sendAudio(SendAudio sendAudio) {
        if (StringUtils.isNotBlank(sendAudio.getCaption())) {
            sendAudio.setParseMode(ParseMode.HTML);
        }

        telegramLocalBotApiService.sendAudio(sendAudio);
    }

    @Override
    public MediaType getMediaType(String fileId) {
        return MediaType.DOCUMENT;
    }

    @Override
    public void sendFile(long chatId, String fileId) {
        sendFile(chatId, fileId, null);
    }

    @Override
    public void sendFile(long chatId, String fileId, String caption) {
        MediaType mediaType = getMediaType(fileId);

        switch (mediaType) {
            case PHOTO:
                sendPhoto(new SendPhoto(chatId, fileId).setCaption(caption));
                break;
            case VIDEO:
                sendVideo(new SendVideo(chatId, fileId).setCaption(caption));
                break;
            case AUDIO:
                sendAudio(new SendAudio(chatId, fileId).setCaption(caption));
                break;
            default:
                sendDocument(new SendDocument(chatId, fileId).setCaption(caption));
                break;
        }
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker) {
        Message message = telegramLocalBotApiService.sendSticker(sendSticker);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }
}
