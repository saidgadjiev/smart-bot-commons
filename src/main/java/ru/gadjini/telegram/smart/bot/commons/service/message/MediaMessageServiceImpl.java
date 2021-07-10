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
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMediaService;

@Service
@Qualifier("media")
@SuppressWarnings("PMD")
public class MediaMessageServiceImpl implements MediaMessageService {

    private MessageMediaService fileService;

    private TelegramMediaService telegramMediaService;

    @Autowired
    public MediaMessageServiceImpl(MessageMediaService fileService,
                                   TelegramMediaService telegramMediaService) {
        this.fileService = fileService;
        this.telegramMediaService = telegramMediaService;
    }

    @Override
    public EditMediaResult editMessageMedia(EditMessageMedia editMessageMedia) {
        if (StringUtils.isNotBlank(editMessageMedia.getMedia().getCaption())) {
            editMessageMedia.getMedia().setParseMode(ParseMode.HTML);
        }
        Message message = telegramMediaService.editMessageMedia(editMessageMedia);

        return new EditMediaResult(fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        if (StringUtils.isNotBlank(sendDocument.getCaption())) {
            sendDocument.setParseMode(ParseMode.HTML);
        }

        sendDocument.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendDocument(sendDocument, progress);

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
            return;
        } catch (Exception ignore) {
        }
        try {
            sendAudio(SendAudio.builder().chatId(String.valueOf(chatId)).audio(new InputFile(fileId)).build());
            return;
        } catch (Exception ignore) {
        }
        try {
            sendVoice(SendVoice.builder().chatId(String.valueOf(chatId)).voice(new InputFile(fileId)).build());
            return;
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
        Message message = telegramMediaService.sendPhoto(sendPhoto);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendVideo(SendVideo sendVideo, Progress progress) {
        if (StringUtils.isNotBlank(sendVideo.getCaption())) {
            sendVideo.setParseMode(ParseMode.HTML);
        }
        sendVideo.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendVideo(sendVideo, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendVideoNote(SendVideoNote sendVideoNote, Progress progress) {
        sendVideoNote.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendVideoNote(sendVideoNote, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendAudio(SendAudio sendAudio, Progress progress) {
        if (StringUtils.isNotBlank(sendAudio.getCaption())) {
            sendAudio.setParseMode(ParseMode.HTML);
        }

        sendAudio.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendAudio(sendAudio, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendVoice(SendVoice sendVoice, Progress progress) {
        if (StringUtils.isNotBlank(sendVoice.getCaption())) {
            sendVoice.setParseMode(ParseMode.HTML);
        }

        sendVoice.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendVoice(sendVoice, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker, Progress progress) {
        sendSticker.setAllowSendingWithoutReply(true);
        Message message = telegramMediaService.sendSticker(sendSticker, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }
}
