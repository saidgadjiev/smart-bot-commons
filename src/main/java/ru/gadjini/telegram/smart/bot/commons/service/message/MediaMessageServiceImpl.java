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
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
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
        editMessageMedia.setMedia(fixInputMediaFileName(editMessageMedia.getMedia()));
        Message message = telegramLocalBotApiService.editMessageMedia(editMessageMedia);

        return new EditMediaResult(fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        if (StringUtils.isNotBlank(sendDocument.getCaption())) {
            sendDocument.setParseMode(ParseMode.HTML);
        }

        sendDocument.setDocument(fixInputFileName(sendDocument.getDocument()));
        Message message = telegramLocalBotApiService.sendDocument(sendDocument, progress);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        sendPhoto.setPhoto(fixInputFileName(sendPhoto.getPhoto()));
        Message message = telegramLocalBotApiService.sendPhoto(sendPhoto);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    @Override
    public void sendVideo(SendVideo sendVideo) {
        if (StringUtils.isNotBlank(sendVideo.getCaption())) {
            sendVideo.setParseMode(ParseMode.HTML);
        }
        sendVideo.setVideo(fixInputFileName(sendVideo.getVideo()));
        telegramLocalBotApiService.sendVideo(sendVideo);
    }

    @Override
    public void sendAudio(SendAudio sendAudio) {
        if (StringUtils.isNotBlank(sendAudio.getCaption())) {
            sendAudio.setParseMode(ParseMode.HTML);
        }

        sendAudio.setAudio(fixInputFileName(sendAudio.getAudio()));
        telegramLocalBotApiService.sendAudio(sendAudio);
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker) {
        sendSticker.setSticker(fixInputFileName(sendSticker.getSticker()));
        Message message = telegramLocalBotApiService.sendSticker(sendSticker);

        return new SendFileResult(message.getMessageId(), fileService.getFileId(message));
    }

    private InputFile fixInputFileName(InputFile inputFile) {
        if (inputFile.isNew()) {
            InputFile result = new InputFile();
            result.setMedia(inputFile.getNewMediaFile(), getFixedFileName(inputFile.getMediaName(), inputFile.getNewMediaFile().getName()));

            return result;
        }

        return inputFile;
    }

    private InputMedia fixInputMediaFileName(InputMedia inputMedia) {
        if (inputMedia.isNewMedia()) {
            InputMedia result = new InputMediaDocument();
            result.setMedia(inputMedia.getNewMediaFile(), getFixedFileName(inputMedia.getMediaName(), inputMedia.getNewMediaFile().getName()));

            return result;
        }

        return inputMedia;
    }

    private String getFixedFileName(String mediaName, String fileName) {
        return StringUtils.defaultIfBlank(mediaName, fileName).replace(";", "");
    }
}
