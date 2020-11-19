package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

@Service
@Qualifier("media")
@SuppressWarnings("PMD")
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
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        if (StringUtils.isNotBlank(sendDocument.getCaption())) {
            sendDocument.setParseMode(ParseMode.HTML);
        }

        sendDocument.setAllowSendingWithoutReply(true);
        Message message = telegramLocalBotApiService.sendDocument(sendDocument, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public void sendFile(long chatId, String fileId) {
        try {
            sendDocument(new SendDocument(String.valueOf(chatId), new InputFile(fileId)));
            return;
        } catch (Exception ignore) {
        }
        try {
            sendVideo(new SendVideo(String.valueOf(chatId), new InputFile(fileId)));
            return;
        } catch (Exception ignore) {
        }
        try {
            sendPhoto(new SendPhoto(String.valueOf(chatId), new InputFile(fileId)));
        } catch (Exception ignore) {

        }
        try {
            sendAudio(SendAudio.builder().chatId(String.valueOf(chatId)).audio(new InputFile(fileId)).build());
        } catch (Exception ignore) {

        }
        try {
            sendSticker(new SendSticker(String.valueOf(chatId), new InputFile(fileId)));
        } catch (Exception ignore) {

        }
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        sendPhoto.setAllowSendingWithoutReply(true);
        Message message = telegramLocalBotApiService.sendPhoto(sendPhoto);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public void sendVideo(SendVideo sendVideo) {
        if (StringUtils.isNotBlank(sendVideo.getCaption())) {
            sendVideo.setParseMode(ParseMode.HTML);
        }
        sendVideo.setAllowSendingWithoutReply(true);
        telegramLocalBotApiService.sendVideo(sendVideo);
    }

    @Override
    public SendFileResult sendAudio(SendAudio sendAudio) {
        if (StringUtils.isNotBlank(sendAudio.getCaption())) {
            sendAudio.setParseMode(ParseMode.HTML);
        }

        sendAudio.setAllowSendingWithoutReply(true);
        Message message = telegramLocalBotApiService.sendAudio(sendAudio);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker) {
        sendSticker.setAllowSendingWithoutReply(true);
        Message message = telegramLocalBotApiService.sendSticker(sendSticker);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }
}
