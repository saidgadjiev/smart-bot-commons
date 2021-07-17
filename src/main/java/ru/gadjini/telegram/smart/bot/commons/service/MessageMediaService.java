package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.domain.FileSource;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;
import ru.gadjini.telegram.smart.bot.commons.service.format.FormatService;
import ru.gadjini.telegram.smart.bot.commons.utils.MimeTypeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.UrlUtils;

import java.util.Comparator;
import java.util.Locale;

@Service
public class MessageMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageMediaService.class);

    private LocalisationService localisationService;

    private FormatService formatService;

    private RestTemplate restTemplate;

    @Autowired
    public MessageMediaService(LocalisationService localisationService, FormatService formatService, RestTemplate restTemplate) {
        this.localisationService = localisationService;
        this.formatService = formatService;
        this.restTemplate = restTemplate;
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
        } else if (message.hasVideoNote()) {
            return message.getVideoNote().getFileId();
        } else if (message.hasVoice()) {
            return message.getVoice().getFileId();
        }

        return null;
    }

    public MessageMedia getMedia(Message message, Locale locale) {
        MessageMedia messageMedia = new MessageMedia();

        if (message.hasDocument()) {
            messageMedia.setFileName(MessageMedia.getFixedFileName(message.getDocument().getFileName()));
            messageMedia.setFileId(message.getDocument().getFileId());
            messageMedia.setMimeType(message.getDocument().getMimeType());
            messageMedia.setFileSize(message.getDocument().getFileSize());
            messageMedia.setSource(FileSource.DOCUMENT);
            PhotoSize thumb = message.getDocument().getThumb();
            if (thumb != null) {
                messageMedia.setThumb(thumb.getFileId());
                messageMedia.setThumbFileSize(thumb.getFileSize());
            }
            messageMedia.setFormat(formatService.getFormat(messageMedia.getFileName(), messageMedia.getMimeType()));

            return messageMedia;
        } else if (message.hasPhoto()) {
            messageMedia.setFileName(localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + ".jpg");
            PhotoSize photoSize = message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getWidth)).orElseThrow();
            messageMedia.setFileId(photoSize.getFileId());
            messageMedia.setMimeType("image/jpeg");
            messageMedia.setFileSize(photoSize.getFileSize());
            messageMedia.setFormat(Format.JPG);
            messageMedia.setSource(FileSource.PHOTO);

            return messageMedia;
        } else if (message.hasVideo()) {
            Format format = formatService.getFormat(MessageMedia.getFixedFileName(message.getVideo().getFileName()), message.getVideo().getMimeType());
            if (format == null) {
                format = Format.MP4;
            }
            String fileName = MessageMedia.getFixedFileName(message.getVideo().getFileName());
            if (StringUtils.isBlank(fileName)) {
                fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + format.getExt();
            }
            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getVideo().getFileId());
            messageMedia.setFileSize(message.getVideo().getFileSize());

            PhotoSize thumb = message.getVideo().getThumb();
            if (thumb != null) {
                messageMedia.setThumb(thumb.getFileId());
                messageMedia.setThumbFileSize(thumb.getFileSize());
            }

            messageMedia.setMimeType(message.getVideo().getMimeType());
            messageMedia.setFormat(format);
            messageMedia.setSource(FileSource.VIDEO);
            messageMedia.setDuration(message.getVideo().getDuration());
            messageMedia.setWidth(message.getVideo().getWidth());
            messageMedia.setHeight(message.getVideo().getHeight());

            return messageMedia;
        } else if (message.hasVideoNote()) {
            String fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + Format.MP4.getExt();

            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getVideoNote().getFileId());
            messageMedia.setFileSize(message.getVideoNote().getFileSize());

            PhotoSize thumb = message.getVideoNote().getThumb();
            if (thumb != null) {
                messageMedia.setThumb(thumb.getFileId());
                messageMedia.setThumbFileSize(thumb.getFileSize());
            }

            messageMedia.setMimeType("video/mp4");
            messageMedia.setFormat(Format.MP4);
            messageMedia.setSource(FileSource.VIDEO_NOTE);
            messageMedia.setDuration(message.getVideoNote().getDuration());

            return messageMedia;
        } else if (message.hasAudio()) {
            Format format = formatService.getFormat(MessageMedia.getFixedFileName(message.getAudio().getFileName()), message.getAudio().getMimeType());

            if (format == null) {
                format = Format.MP3;
            }
            String fileName = MessageMedia.getFixedFileName(message.getAudio().getFileName());
            if (StringUtils.isBlank(fileName)) {
                fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + format.getExt();
            }
            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getAudio().getFileId());
            messageMedia.setMimeType(message.getAudio().getMimeType());
            messageMedia.setFileSize(message.getAudio().getFileSize());

            PhotoSize thumb = message.getAudio().getThumb();
            if (thumb != null) {
                messageMedia.setThumb(thumb.getFileId());
                messageMedia.setThumbFileSize(thumb.getFileSize());
            }

            messageMedia.setAudioPerformer(message.getAudio().getPerformer());
            messageMedia.setAudioTitle(message.getAudio().getTitle());
            messageMedia.setSource(FileSource.AUDIO);
            messageMedia.setDuration(message.getAudio().getDuration());
            messageMedia.setFormat(format);

            return messageMedia;
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            messageMedia.setFileId(sticker.getFileId());
            String fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + (sticker.getIsAnimated() ? "tgs" : "webp");
            messageMedia.setFileName(fileName);
            messageMedia.setMimeType(sticker.getIsAnimated() ? null : "image/webp");
            messageMedia.setFileSize(message.getSticker().getFileSize());
            messageMedia.setFormat(sticker.getIsAnimated() ? Format.TGS : Format.WEBP);
            messageMedia.setSource(FileSource.STICKER);

            return messageMedia;
        } else if (message.hasVoice()) {
            Format format = formatService.getFormat(null, message.getVoice().getMimeType());
            if (format == null) {
                LOGGER.debug("Voice format null({}, {})", message.getVoice().getMimeType(), message.getVoice().getFileId());
                format = Format.OGG;
            }
            String fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + format.getExt();

            messageMedia.setFileName(fileName);
            messageMedia.setFileId(message.getVoice().getFileId());
            messageMedia.setMimeType(message.getVoice().getMimeType());
            messageMedia.setFileSize(message.getVoice().getFileSize());
            messageMedia.setDuration(message.getVoice().getDuration());
            messageMedia.setSource(FileSource.VOICE);
            messageMedia.setFormat(format);

            return messageMedia;
        } else if (message.hasText()) {
            Format format = formatService.getFormat(message.getText());

            if (Format.URL.equals(format)) {
                try {
                    String url = UrlUtils.appendScheme(message.getText());
                    HttpHeaders httpHeaders = restTemplate.headForHeaders(url);
                    if (httpHeaders.getContentType() == null || StringUtils.isBlank(httpHeaders.getContentType().getType())) {
                        throw new IllegalArgumentException("Empty content type");
                    }
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String mimeType = MimeTypeUtils.removeCharset(httpHeaders.getContentType().toString());
                    Format mediaFormat = formatService.getFormat(fileName, mimeType);
                    if (mediaFormat == null) {
                        throw new IllegalArgumentException("Unknown media type " + mimeType);
                    }
                    if (httpHeaders.getContentLength() <= 0
                            || httpHeaders.getContentLength() > TgConstants.LARGE_FILE_SIZE) {
                        throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BIG_IN_REMOTE_FILE, locale));
                    }
                    if (StringUtils.isBlank(fileName)) {
                        fileName = localisationService.getMessage(MessagesProperties.MESSAGE_EMPTY_FILE_NAME, locale) + "." + mediaFormat.getExt();
                    }

                    messageMedia.setFileName(fileName);
                    messageMedia.setFileId(url);
                    messageMedia.setMimeType(mimeType);
                    messageMedia.setFileSize(httpHeaders.getContentLength());
                    messageMedia.setFormat(mediaFormat);

                    return messageMedia;
                } catch (UserException e) {
                    throw e;
                } catch (Throwable e) {
                    LOGGER.error("Incorrect url({}, {})", message.getText(), e.getMessage());
                    throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_INCORRECT_MEDIA_URL, locale));
                }
            }
        }

        return null;
    }
}
