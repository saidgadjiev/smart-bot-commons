package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.SendFileResult;
import ru.gadjini.telegram.smart.bot.commons.service.flood.UploadFloodWaitController;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

@Service
public class FileUploader {

    private TelegramBotApiService telegramBotApiService;

    private MediaMessageService mediaMessageService;

    private UploadFloodWaitController uploadFloodWaitController;

    @Autowired
    public FileUploader(TelegramBotApiService telegramBotApiService,
                        @Qualifier("mediaLimits") MediaMessageService mediaMessageService, UploadFloodWaitController uploadFloodWaitController) {
        this.telegramBotApiService = telegramBotApiService;
        this.mediaMessageService = mediaMessageService;
        this.uploadFloodWaitController = uploadFloodWaitController;
    }

    public SendFileResult upload(String method, Object body, Progress progress) {
        String key = getFilePathOrFileId(method, body);
        uploadFloodWaitController.startUploading(key);
        try {
            return doUpload(method, body, progress);
        } finally {
            uploadFloodWaitController.finishUploading(key);
        }
    }

    public void cancelUploading(String method, Object body) {
        String key = getFilePathOrFileId(method, body);
        uploadFloodWaitController.cancelUploading(key);
        String filePath = getFilePath(method, body);

        if (StringUtils.isNotBlank(filePath)) {
            telegramBotApiService.cancelUploading(filePath);
        }
    }

    private String getFilePath(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);

        if (inputFile.isNew()) {
            return inputFile.getNewMediaFile().getAbsolutePath();
        }

        return null;
    }

    private String getFilePathOrFileId(String method, Object body) {
        InputFile inputFile = getInputFile(method, body);

        if (inputFile.isNew()) {
            return inputFile.getNewMediaFile().getAbsolutePath();
        }

        return inputFile.getAttachName();
    }

    private InputFile getInputFile(String method, Object body) {
        InputFile inputFile = null;
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                inputFile = sendDocument.getDocument();
                break;
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                inputFile = sendAudio.getAudio();
                break;
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                inputFile = sendVideo.getVideo();
                break;
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                inputFile = sendVoice.getVoice();
                break;
            }
        }
        if (inputFile == null) {
            throw new IllegalArgumentException("Null input file " + body);
        }

        return inputFile;
    }

    private SendFileResult doUpload(String method, Object body, Progress progress) {
        switch (method) {
            case SendDocument.PATH: {
                SendDocument sendDocument = (SendDocument) body;
                return mediaMessageService.sendDocument(sendDocument, progress);
            }
            case SendAudio.PATH: {
                SendAudio sendAudio = (SendAudio) body;
                return mediaMessageService.sendAudio(sendAudio, progress);
            }
            case SendVideo.PATH: {
                SendVideo sendVideo = (SendVideo) body;
                return mediaMessageService.sendVideo(sendVideo, progress);
            }
            case SendVoice.PATH: {
                SendVoice sendVoice = (SendVoice) body;
                return mediaMessageService.sendVoice(sendVoice, progress);
            }
        }

        throw new IllegalArgumentException("Unsupported method to upload " + method);
    }
}
