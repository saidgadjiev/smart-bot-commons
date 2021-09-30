package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.LocalBotApi;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApi;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApiBalancer;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.TelegramBotApiBalancerProperties;

@Component
@TelegramBotApiBalancer
public class TelegramBotApiMediaServiceBalancer implements TelegramMediaService {

    private TelegramMediaService localBotApiService;

    private TelegramMediaService telegramBotApiService;

    private TelegramBotApiBalancerProperties botApiBalancerProperties;

    @Autowired
    public TelegramBotApiMediaServiceBalancer(@LocalBotApi TelegramMediaService localBotApiService,
                                              @TelegramBotApi TelegramMediaService telegramBotApiService,
                                              TelegramBotApiBalancerProperties botApiBalancerProperties) {
        this.localBotApiService = localBotApiService;
        this.telegramBotApiService = telegramBotApiService;
        this.botApiBalancerProperties = botApiBalancerProperties;
    }

    @Override
    public Message sendAnimation(SendAnimation sendAnimation, Progress progress) {
        if (sendAnimation.getAnimation().isNew()) {
            return getUploadingService(sendAnimation.getAnimation().getNewMediaFile().length()).sendAnimation(sendAnimation, progress);
        }

        return localBotApiService.sendAnimation(sendAnimation, progress);
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        if (editMessageMedia.getMedia().isNewMedia()) {
            return getUploadingService(editMessageMedia.getMedia().getNewMediaFile().length()).editMessageMedia(editMessageMedia);
        }

        return localBotApiService.editMessageMedia(editMessageMedia);
    }

    @Override
    public Message sendSticker(SendSticker sendSticker, Progress progress) {
        if (sendSticker.getSticker().isNew()) {
            return getUploadingService(sendSticker.getSticker().getNewMediaFile().length()).sendSticker(sendSticker, progress);
        }

        return localBotApiService.sendSticker(sendSticker, progress);
    }

    @Override
    public Message sendDocument(SendDocument sendDocument, Progress progress) {
        if (sendDocument.getDocument().isNew()) {
            return getUploadingService(sendDocument.getDocument().getNewMediaFile().length()).sendDocument(sendDocument, progress);
        }

        return localBotApiService.sendDocument(sendDocument, progress);
    }

    @Override
    public Message sendVideo(SendVideo sendVideo, Progress progress) {
        if (sendVideo.getVideo().isNew()) {
            return getUploadingService(sendVideo.getVideo().getNewMediaFile().length()).sendVideo(sendVideo, progress);
        }

        return localBotApiService.sendVideo(sendVideo, progress);
    }

    @Override
    public Message sendVideoNote(SendVideoNote sendVideoNote, Progress progress) {
        if (sendVideoNote.getVideoNote().isNew()) {
            return getUploadingService(sendVideoNote.getVideoNote().getNewMediaFile().length()).sendVideoNote(sendVideoNote, progress);
        }

        return localBotApiService.sendVideoNote(sendVideoNote, progress);
    }

    @Override
    public Message sendAudio(SendAudio sendAudio, Progress progress) {
        if (sendAudio.getAudio().isNew()) {
            return getUploadingService(sendAudio.getAudio().getNewMediaFile().length()).sendAudio(sendAudio, progress);
        }

        return localBotApiService.sendAudio(sendAudio, progress);
    }

    @Override
    public Message sendVoice(SendVoice sendVoice, Progress progress) {
        if (sendVoice.getVoice().isNew()) {
            return getUploadingService(sendVoice.getVoice().getNewMediaFile().length()).sendVoice(sendVoice, progress);
        }

        return localBotApiService.sendVoice(sendVoice, progress);
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        if (sendPhoto.getPhoto().isNew()) {
            return getUploadingService(sendPhoto.getPhoto().getNewMediaFile().length()).sendPhoto(sendPhoto);
        }

        return localBotApiService.sendPhoto(sendPhoto);
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        return getDownloadingService(fileSize).downloadFileByFileId(fileId, fileSize, outputFile);
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        return getDownloadingService(fileSize).downloadFileByFileId(fileId, fileSize, progress, outputFile);
    }

    @Override
    public void cancelUploading(String filePath) {
        localBotApiService.cancelUploading(filePath);
        telegramBotApiService.cancelUploading(filePath);
    }

    @Override
    public void cancelDownloading(String fileId) {
        localBotApiService.cancelDownloading(fileId);
        telegramBotApiService.cancelDownloading(fileId);
    }

    @Override
    public void cancelDownloads() {
        localBotApiService.cancelDownloads();
        telegramBotApiService.cancelDownloads();
    }

    private TelegramMediaService getDownloadingService(long fileSize) {
        if (fileSize <= 0) {
            return localBotApiService;
        }
        if (fileSize > botApiBalancerProperties.getDownloadingLightFileMaxSize()) {
            return localBotApiService;
        }

        return telegramBotApiService;
    }

    private TelegramMediaService getUploadingService(long fileSize) {
        if (fileSize <= 0) {
            return localBotApiService;
        }
        if (fileSize > botApiBalancerProperties.getUploadingLightFileMaxSize()) {
            return localBotApiService;
        }

        return telegramBotApiService;
    }
}
