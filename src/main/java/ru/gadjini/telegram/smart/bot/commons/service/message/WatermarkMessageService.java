package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.annotation.WatermarkMessages;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.MessageProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.TelegramLinkUtils;

@Service
@WatermarkMessages
public class WatermarkMessageService implements MediaMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatermarkMessageService.class);

    private MessageProperties messageProperties;

    private MediaMessageService mediaMessageService;

    private BotProperties botProperties;

    @Autowired
    public WatermarkMessageService(MessageProperties messageProperties, BotProperties botProperties) {
        this.messageProperties = messageProperties;
        this.botProperties = botProperties;

        LOGGER.debug("Watermark referral({})", messageProperties.getReferral());
    }

    @Autowired
    public void setMediaMessageService(@Qualifier("media") MediaMessageService mediaMessageService) {
        this.mediaMessageService = mediaMessageService;
    }

    @Override
    public EditMediaResult editMessageMedia(EditMessageMedia editMessageMedia) {
        return mediaMessageService.editMessageMedia(editMessageMedia);
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker, Progress progress) {
        return mediaMessageService.sendSticker(sendSticker, progress);
    }

    @Override
    public void sendFile(long chatId, String fileId) {
        mediaMessageService.sendFile(chatId, fileId);
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        return mediaMessageService.sendPhoto(sendPhoto);
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        sendDocument.setCaption(appendWatermark(sendDocument.getChatId(), sendDocument.getCaption()));

        return mediaMessageService.sendDocument(sendDocument, progress);
    }

    @Override
    public SendFileResult sendVideo(SendVideo sendVideo, Progress progress) {
        sendVideo.setCaption(appendWatermark(sendVideo.getChatId(), sendVideo.getCaption()));

        return mediaMessageService.sendVideo(sendVideo, progress);
    }

    @Override
    public SendFileResult sendVideoNote(SendVideoNote sendVideoNote, Progress progress) {
        return mediaMessageService.sendVideoNote(sendVideoNote, progress);
    }

    @Override
    public SendFileResult sendAudio(SendAudio sendAudio, Progress progress) {
        sendAudio.setCaption(appendWatermark(sendAudio.getChatId(), sendAudio.getCaption()));
        return mediaMessageService.sendAudio(sendAudio, progress);
    }

    @Override
    public SendFileResult sendVoice(SendVoice sendVoice, Progress progress) {
        sendVoice.setCaption(appendWatermark(sendVoice.getChatId(), sendVoice.getCaption()));
        return mediaMessageService.sendVoice(sendVoice, progress);
    }

    private String appendWatermark(String chatId, String text) {
        String watermarkReferral = messageProperties.getWatermarkReferral(chatId);

        if (StringUtils.isNotBlank(watermarkReferral)) {
            String botLink = TelegramLinkUtils.botLink(botProperties.getName(), watermarkReferral);

            return StringUtils.isBlank(text) ? botLink : text + "\n\n" + botLink;
        }

        return text;
    }
}
