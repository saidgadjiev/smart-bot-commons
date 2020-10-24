package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.model.EditMediaResult;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.MediaType;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.*;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.property.FileLimitProperties;

@Component
@Qualifier("forceMedia")
public class ForceMediaMessageService implements MediaMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceMediaMessageService.class);

    private MediaMessageService mediaMessageService;

    @Autowired
    public void setMediaMessageService(@Qualifier("mediaLimits") MediaMessageService mediaMessageService) {
        this.mediaMessageService = mediaMessageService;
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
    public SendFileResult sendDocument(SendDocument sendDocument) {
        int attempts = 0;
        Throwable lastEx = null;
        while (attempts < FileLimitProperties.FLOOD_WAIT_MAX_ATTEMPTS) {
            ++attempts;
            try {
                return mediaMessageService.sendDocument(sendDocument);
            } catch (Throwable ex) {
                lastEx = ex;
                int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, FloodWaitException.class);
                if (floodWaitExceptionIndexOf == -1) {
                    throw ex;
                } else {
                    LOGGER.debug("Attemp({}, {})", attempts, ex.getMessage());
                    try {
                        Thread.sleep(FileLimitProperties.SLEEP_TIME_BEFORE_ATTEMPT);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        throw new TelegramApiException(lastEx);
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

    @Override
    public MediaType getMediaType(String fileId) {
        return mediaMessageService.getMediaType(fileId);
    }

    @Override
    public void sendFile(long chatId, String fileId) {
        mediaMessageService.sendFile(chatId, fileId);
    }

    @Override
    public void sendFile(long chatId, String fileId, String caption) {
        mediaMessageService.sendFile(chatId, fileId, caption);
    }
}
