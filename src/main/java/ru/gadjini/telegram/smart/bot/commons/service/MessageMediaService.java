package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.PhotoSize;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Sticker;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.service.format.FormatService;

import java.util.Comparator;
import java.util.Locale;

@Service
public class MessageMediaService {

    private LocalisationService localisationService;

    private FormatService formatService;

    @Autowired
    public MessageMediaService(LocalisationService localisationService, FormatService formatService) {
        this.localisationService = localisationService;
        this.formatService = formatService;
    }

    public String getFileId(Message message) {
        if (message.hasDocument()) {
            return message.getDocument().getFileId();
        } else if (message.hasPhoto()) {
            PhotoSize photoSize = message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getWidth)).orElseThrow();

            return photoSize.getFileId();
        } else if (message.hasVideo()) {
            return message.getVideo().getFileId();
        } else if (message.hasAudio()) {
            return message.getAudio().getFileId();
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();

            return sticker.getFileId();
        }

        return null;
    }

    public MessageMedia getMedia(Message message, Locale locale) {
        MessageMedia messageMedia = new MessageMedia();

        if (message.hasDocument()) {
            messageMedia.setFileName(message.getDocument().getFileName());
            messageMedia.setFileId(message.getDocument().getFileId());
            messageMedia.setMimeType(message.getDocument().getMimeType());
            messageMedia.setFileSize(message.getDocument().getFileSize());
            messageMedia.setThumb(message.getDocument().hasThumb() ? message.getDocument().getThumb().getFileId() : null);
            messageMedia.setFormat(formatService.getFormat(messageMedia.getFileName(), messageMedia.getMimeType()));

            return messageMedia;
        } else if (message.hasPhoto()) {
            messageMedia.setFileName(localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + ".jpg");
            PhotoSize photoSize = message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getWidth)).orElseThrow();
            messageMedia.setFileId(photoSize.getFileId());
            messageMedia.setMimeType("image/jpeg");
            messageMedia.setFileSize(photoSize.getFileSize());
            messageMedia.setFormat(Format.JPG);

            return messageMedia;
        } else if (message.hasVideo()) {
            String fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + ".";
            Format format = formatService.getFormat(message.getVideo().getFileName(), message.getVideo().getMimeType());
            if (format == null) {
                format = Format.MP4;
            }
            fileName += format.getExt();
            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getVideo().getFileId());
            messageMedia.setFileSize(message.getVideo().getFileSize());
            messageMedia.setFileName(message.getVideo().getFileName());
            messageMedia.setThumb(message.getVideo().hasThumb() ? message.getVideo().getThumb().getFileId() : null);
            messageMedia.setMimeType(message.getVideo().getMimeType());
            messageMedia.setFormat(format);

            return messageMedia;
        } else if (message.hasAudio()) {
            String fileName = message.getAudio().getFileName();
            Format format = formatService.getFormat(message.getAudio().getFileName(), message.getAudio().getMimeType());

            if (StringUtils.isBlank(fileName)) {
                fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + ".";
                fileName += format.getExt();
            }
            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getAudio().getFileId());
            messageMedia.setMimeType(message.getAudio().getMimeType());
            messageMedia.setFileSize(message.getAudio().getFileSize());
            messageMedia.setFileName(message.getAudio().getFileName());
            messageMedia.setThumb(message.getAudio().hasThumb() ? message.getAudio().getThumb().getFileId() : null);
            messageMedia.setFormat(format);

            return messageMedia;
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            messageMedia.setFileId(sticker.getFileId());
            String fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + ".";
            fileName += sticker.getAnimated() ? "tgs" : "webp";
            messageMedia.setFileName(fileName);
            messageMedia.setMimeType(sticker.getAnimated() ? null : "image/webp");
            messageMedia.setFileSize(message.getSticker().getFileSize());
            messageMedia.setFormat(sticker.getAnimated() ? Format.TGS : Format.WEBP);

            return messageMedia;
        }

        return null;
    }
}
