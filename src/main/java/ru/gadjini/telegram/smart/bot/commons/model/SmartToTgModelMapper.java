package ru.gadjini.telegram.smart.bot.commons.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.File;

@Service
public class SmartToTgModelMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public SmartToTgModelMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EditMessageMedia map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageMedia request) {
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(request.getChatId());
        editMessageMedia.setMessageId(request.getMessageId());
        editMessageMedia.setReplyMarkup(objectMapper.convertValue(request.getReplyMarkup(), InlineKeyboardMarkup.class));

        editMessageMedia.setMedia(map(request.getMedia()));

        return editMessageMedia;
    }

    public SendSticker map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendSticker request) {
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(request.getChatId());
        sendSticker.setReplyMarkup(objectMapper.convertValue(request.getReplyMarkup(), InlineKeyboardMarkup.class));
        sendSticker.setReplyToMessageId(request.getReplyToMessageId());
        sendSticker.setSticker(map(request.getSticker()));

        return sendSticker;
    }

    public SendPhoto map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendPhoto request) {
        SendPhoto sendSticker = new SendPhoto();
        sendSticker.setChatId(request.getChatId());
        sendSticker.setPhoto(map(request.getPhoto()));

        return sendSticker;
    }

    public SendVideo map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendVideo request) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(request.getChatId());
        sendVideo.setCaption(request.getCaption());
        sendVideo.setReplyToMessageId(request.getReplyToMessageId());
        sendVideo.setReplyMarkup(objectMapper.convertValue(request.getReplyMarkup(), InlineKeyboardMarkup.class));
        sendVideo.setVideo(map(request.getVideo()));
        sendVideo.setParseMode(request.getParseMode());

        if (StringUtils.isNotBlank(request.getVideo().getThumb())) {
            InputFile thumb = new InputFile();
            File thumbFile = new File(request.getVideo().getThumb());
            thumb.setMedia(thumbFile, thumbFile.getName());
            sendVideo.setThumb(thumb);
        }

        return sendVideo;
    }

    public SendDocument map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendDocument request) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(request.getChatId());
        sendDocument.setCaption(request.getCaption());
        sendDocument.setReplyToMessageId(request.getReplyToMessageId());
        sendDocument.setReplyMarkup(objectMapper.convertValue(request.getReplyMarkup(), InlineKeyboardMarkup.class));
        sendDocument.setDocument(map(request.getDocument()));
        sendDocument.setParseMode(request.getParseMode());

        if (StringUtils.isNotBlank(request.getDocument().getThumb())) {
            InputFile thumb = new InputFile();
            File thumbFile = new File(request.getDocument().getThumb());
            thumb.setMedia(thumbFile, thumbFile.getName());
            sendDocument.setThumb(thumb);
        }

        return sendDocument;
    }

    public SendAudio map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendAudio request) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(request.getChatId());
        sendAudio.setCaption(request.getCaption());
        sendAudio.setReplyToMessageId(request.getReplyToMessageId());
        sendAudio.setReplyMarkup(objectMapper.convertValue(request.getReplyMarkup(), InlineKeyboardMarkup.class));
        sendAudio.setAudio(map(request.getAudio()));
        sendAudio.setParseMode(request.getParseMode());

        if (StringUtils.isNotBlank(request.getAudio().getThumb())) {
            InputFile thumb = new InputFile();
            File thumbFile = new File(request.getAudio().getThumb());
            thumb.setMedia(thumbFile, thumbFile.getName());
            sendAudio.setThumb(thumb);
        }

        return sendAudio;
    }

    public InputFile map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.InputFile request) {
        InputFile inputFile = new InputFile();
        if (StringUtils.isNotBlank(request.getFileId())) {
            inputFile.setMedia(request.getFileId());
        } else {
            File file = new File(request.getFilePath());
            inputFile.setMedia(file, StringUtils.defaultIfBlank(request.getFileName(), file.getName()).replace(";", ""));
        }

        return inputFile;
    }

    public InputMedia map(ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.InputMedia request) {
        InputMedia inputMedia = new InputMediaDocument();
        inputMedia.setCaption(request.getCaption());
        if (StringUtils.isNotBlank(request.getFileId())) {
            inputMedia.setMedia(request.getFileId());
        }
        inputMedia.setParseMode(request.getParseMode());

        if (StringUtils.isNotBlank(request.getFilePath())) {
            File file = new File(request.getFilePath());
            inputMedia.setMedia(file, StringUtils.defaultIfBlank(request.getFileName(), file.getName()).replace(";", ""));
        }

        return inputMedia;
    }
}
