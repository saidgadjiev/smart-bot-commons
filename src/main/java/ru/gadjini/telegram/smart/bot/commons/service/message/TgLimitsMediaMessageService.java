package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.io.File;
import java.util.Arrays;

@Component
@Qualifier("mediaLimits")
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
    public void setMediaMessageService(@Qualifier("media") MediaMessageService mediaMessageService) {
        this.mediaMessageService = mediaMessageService;
    }

    @Autowired
    public void setMediaMessageService(@Qualifier("messageLimits") MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public EditMediaResult editMessageMedia(EditMessageMedia editMediaContext) {
        return mediaMessageService.editMessageMedia(editMediaContext);
    }

    @Override
    public SendFileResult sendSticker(SendSticker sendSticker) {
        return mediaMessageService.sendSticker(sendSticker);
    }

    @Override
    public SendFileResult sendDocument(SendDocument sendDocument, Progress progress) {
        if (validate(sendDocument)) {
            return mediaMessageService.sendDocument(sendDocument, progress);
        }

        return null;
    }

    @Override
    public SendFileResult sendPhoto(SendPhoto sendPhoto) {
        return mediaMessageService.sendPhoto(sendPhoto);
    }

    @Override
    public void sendVideo(SendVideo sendVideo) {
        mediaMessageService.sendVideo(sendVideo);
    }

    @Override
    public void sendAudio(SendAudio sendAudio) {
        mediaMessageService.sendAudio(sendAudio);
    }

    private boolean validate(SendDocument sendDocument) {
        InputFile document = sendDocument.getDocument();
        if (!document.isNew()) {
            return true;
        }
        File file = document.getNewMediaFile();
        if (file.length() == 0) {
            LOGGER.error("Zero file\n{}", Arrays.toString(Thread.currentThread().getStackTrace()));
            messageService.sendMessage(SendMessage.builder().chatId(sendDocument.getChatId())
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_ZERO_LENGTH_FILE,
                            new Object[]{StringUtils.defaultIfBlank(sendDocument.getDocument().getMediaName(), "")},
                            userService.getLocaleOrDefault(Integer.parseInt(sendDocument.getChatId()))))
                    .replyMarkup(sendDocument.getReplyMarkup())
                    .replyToMessageId(sendDocument.getReplyToMessageId())
                    .build());

            return false;
        }
        if (file.length() > TgConstants.LARGE_FILE_SIZE) {
            LOGGER.debug("Large out file({}, {})", sendDocument.getChatId(), MemoryUtils.humanReadableByteCount(file.length()));
            String text = localisationService.getMessage(MessagesProperties.MESSAGE_TOO_LARGE_OUT_FILE,
                    new Object[]{sendDocument.getDocument().getMediaName(), MemoryUtils.humanReadableByteCount(file.length())},
                    userService.getLocaleOrDefault(Integer.parseInt(sendDocument.getChatId())));

            messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(sendDocument.getChatId())).text(text)
                    .replyMarkup(sendDocument.getReplyMarkup())
                    .replyToMessageId(sendDocument.getReplyToMessageId())
                    .build());

            return false;
        } else {
            return true;
        }
    }
}
