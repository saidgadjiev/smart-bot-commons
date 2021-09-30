package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.annotation.TelegramMediaLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.annotation.WatermarkMessages;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.exception.InvalidMediaMessageException;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.io.File;
import java.util.Arrays;

@Component
@TelegramMediaLimitsControl
public class TgLimitsMediaMessageService implements MediaMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TgLimitsMediaMessageService.class);

    private UserService userService;

    private MediaMessageService mediaMessageService;

    private MessageService messageService;

    private LocalisationService localisationService;

    @Autowired
    public TgLimitsMediaMessageService(UserService userService, LocalisationService localisationService) {
        this.userService = userService;
        this.localisationService = localisationService;
    }

    @Autowired
    public void setMediaMessageService(@WatermarkMessages MediaMessageService mediaMessageService) {
        this.mediaMessageService = mediaMessageService;
    }

    @Autowired
    public void setMediaMessageService(@TgMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public SendFileResult sendAnimation(SendAnimation sendAnimation, Progress progress) {
        return mediaMessageService.sendAnimation(sendAnimation, progress);
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
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        if (validate(sendDocument.getChatId(), sendDocument.getDocument(), sendDocument.getReplyMarkup(), sendDocument.getReplyToMessageId())) {
            return mediaMessageService.sendDocument(sendDocument, progress);
        }

        throw new InvalidMediaMessageException();
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
    public SendFileResult sendVideo(SendVideo sendVideo, Progress progress) {
        if (validate(sendVideo.getChatId(), sendVideo.getVideo(), sendVideo.getReplyMarkup(), sendVideo.getReplyToMessageId())) {
            return mediaMessageService.sendVideo(sendVideo, progress);
        }

        throw new InvalidMediaMessageException();
    }

    @Override
    public SendFileResult sendVideoNote(SendVideoNote sendVideoNote, Progress progress) {
        if (validate(sendVideoNote.getChatId(), sendVideoNote.getVideoNote(), sendVideoNote.getReplyMarkup(), sendVideoNote.getReplyToMessageId())) {
            return mediaMessageService.sendVideoNote(sendVideoNote, progress);
        }

        throw new InvalidMediaMessageException();
    }

    @Override
    public SendFileResult sendAudio(SendAudio sendAudio, Progress progress) {
        if (validate(sendAudio.getChatId(), sendAudio.getAudio(), sendAudio.getReplyMarkup(), sendAudio.getReplyToMessageId())) {
            return mediaMessageService.sendAudio(sendAudio, progress);
        }

        throw new InvalidMediaMessageException();
    }

    @Override
    public SendFileResult sendVoice(SendVoice sendVoice, Progress progress) {
        if (validate(sendVoice.getChatId(), sendVoice.getVoice(), sendVoice.getReplyMarkup(), sendVoice.getReplyToMessageId())) {
            return mediaMessageService.sendVoice(sendVoice, progress);
        }

        throw new InvalidMediaMessageException();
    }

    private boolean validate(String chatId, InputFile inputFile, ReplyKeyboard replyKeyboard, Integer replyMessageId) {
        if (!inputFile.isNew()) {
            return true;
        }
        File file = inputFile.getNewMediaFile();
        if (file.length() == 0) {
            LOGGER.error("Zero file({}, {})\n{}", chatId, file.getAbsolutePath(), Arrays.toString(Thread.currentThread().getStackTrace()));
            messageService.sendMessage(SendMessage.builder().chatId(chatId)
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_ZERO_LENGTH_FILE,
                            new Object[]{StringUtils.defaultIfBlank(inputFile.getMediaName(), "")},
                            userService.getLocaleOrDefault(Integer.parseInt(chatId))))
                    .replyMarkup(replyKeyboard)
                    .replyToMessageId(replyMessageId)
                    .build());

            return false;
        }
        if (file.length() > TgConstants.LARGE_FILE_SIZE) {
            LOGGER.debug("Large out file({}, {})", chatId, MemoryUtils.humanReadableByteCount(file.length()));
            String text = localisationService.getMessage(MessagesProperties.MESSAGE_TOO_LARGE_OUT_FILE,
                    new Object[]{inputFile.getMediaName(), MemoryUtils.humanReadableByteCount(file.length())},
                    userService.getLocaleOrDefault(Integer.parseInt(chatId)));

            messageService.sendMessage(SendMessage.builder().chatId(chatId).text(text)
                    .replyMarkup(replyKeyboard)
                    .replyToMessageId(replyMessageId)
                    .build());

            return false;
        } else {
            return true;
        }
    }
}
