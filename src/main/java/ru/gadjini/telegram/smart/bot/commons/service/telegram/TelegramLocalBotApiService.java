package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.exception.UnknownDownloadingUploadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.SmartToTgModelMapper;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.IsChatMember;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.*;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.DeleteMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageCaption;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageReplyMarkup;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.AnswerCallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.GetFile;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.LocalBotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TelegramLocalBotApiService extends DefaultAbsSender implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramLocalBotApiService.class);

    private final BotProperties botProperties;

    private final LocalBotApiProperties localBotApiProperties;

    private ObjectMapper objectMapper;

    private SmartToTgModelMapper modelMapper;

    @Autowired
    public TelegramLocalBotApiService(BotProperties botProperties, ObjectMapper objectMapper,
                                      DefaultBotOptions botOptions, LocalBotApiProperties localBotApiProperties,
                                      SmartToTgModelMapper modelMapper) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
        this.localBotApiProperties = localBotApiProperties;
        this.modelMapper = modelMapper;
    }

    public Boolean isChatMember(IsChatMember isChatMember) {
        return executeWithResult(() -> {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(isChatMember.getChatId());
            getChatMember.setUserId(isChatMember.getUserId());

            ChatMember member = execute(getChatMember);

            return isInGroup(member.getStatus());
        });
    }

    public Boolean sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        return executeWithResult(() -> {
            return execute(objectMapper.convertValue(answerCallbackQuery, org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.class));
        });
    }

    public Message sendMessage(SendMessage sendMessage) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(objectMapper.convertValue(sendMessage, org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    public void editReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        executeWithException(() -> {
            execute(objectMapper.convertValue(editMessageReplyMarkup, org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup.class));
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        executeWithException(() -> {
            execute(objectMapper.convertValue(editMessageText, org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class));
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        executeWithException(() -> {
            execute(objectMapper.convertValue(editMessageCaption, org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption.class));
        });
    }

    public Boolean deleteMessage(DeleteMessage deleteMessage) {
        return executeWithResult(() -> {
            return execute(objectMapper.convertValue(deleteMessage, org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage.class));
        });
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = (org.telegram.telegrambots.meta.api.objects.Message) execute(modelMapper.map(editMessageMedia));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendSticker(SendSticker sendSticker) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendSticker));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendDocument(SendDocument sendDocument) {
        return executeWithResult(() -> {
            updateProgressBeforeStart(sendDocument.getProgress());
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendDocument));

            updateProgressAfterComplete(sendDocument.getProgress());

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendVideo(SendVideo sendVideo) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendVideo));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendAudio(SendAudio sendAudio) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendAudio));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        return executeWithResult(() -> {
            org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendPhoto));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.debug("Start downloadFileByFileId({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));

            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            getFile.setFileSize(fileSize);
            getFile.setPath(outputFile.getAbsolutePath());
            getFile.setRemoveParentDirOnCancel(false);
            if (isShowingProgress(fileSize)) {
                getFile.setProgress(progress);
            }
            executeWithException(() -> {
                updateProgressBeforeStart(getFile.getProgress());
                org.telegram.telegrambots.meta.api.methods.GetFile gf = new org.telegram.telegrambots.meta.api.methods.GetFile();
                gf.setFileId(getFile.getFileId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(gf);
                String filePath = getLocalFilePath(file.getFilePath());

                try {
                    FileUtils.copyFile(new File(filePath), outputFile.getFile());
                } catch (IOException e) {
                    throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
                } finally {
                    FileUtils.deleteQuietly(new File(filePath));
                }
                updateProgressAfterComplete(getFile.getProgress());
            });

            stopWatch.stop();
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {})", fileId, MemoryUtils.humanReadableByteCount(outputFile.length()), stopWatch.getTime(TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new UnknownDownloadingUploadingException(e);
        }
    }

    @Override
    public boolean cancelUploading(String filePath) {
        return false;
    }

    @Override
    public boolean cancelDownloading(String fileId) {
        return false;
    }

    @Override
    public void cancelDownloads() {

    }

    private boolean isShowingProgress(long fileSize) {
        return fileSize > 5 * 1024 * 1024;
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private void executeWithException(Executable executable) {
        try {
            executable.executeWithException();
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e) {
            throw new TelegramApiRequestException(e.getApiResponse());
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            throw new TelegramApiException(e);
        }
    }

    private <V> V executeWithResult(Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e) {
            throw new TelegramApiRequestException(e.getApiResponse());
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            throw new TelegramApiException(e);
        }
    }

    private boolean isInGroup(String status) {
        if (StringUtils.isBlank(status)) {
            return true;
        }
        return Set.of("creator", "administrator", "member", "restricted").contains(status);
    }

    private void updateProgressBeforeStart(Progress progress) {
        if (progress == null) {
            return;
        }
        try {
            org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText editMessageText = new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText();
            editMessageText.setChatId(Long.valueOf(progress.getChatId()));
            editMessageText.setText(progress.getProgressMessage());
            editMessageText.setReplyMarkup(objectMapper.convertValue(progress.getProgressReplyMarkup(), InlineKeyboardMarkup.class));
            editMessageText.setMessageId(progress.getProgressMessageId());
            editMessageText.setParseMode(ParseMode.HTML);

            execute(editMessageText);
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e) {
            LOGGER.error("Error: " + e.getMessage() + " code: " + e.getErrorCode() + " description: " + e.getApiResponse(), e);
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void updateProgressAfterComplete(Progress progress) {
        if (progress == null || StringUtils.isBlank(progress.getAfterProgressCompletionMessage())) {
            return;
        }
        try {
            org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText editMessageText = new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText();
            editMessageText.setChatId(Long.valueOf(progress.getChatId()));
            editMessageText.setText(progress.getAfterProgressCompletionMessage());
            editMessageText.setReplyMarkup(objectMapper.convertValue(progress.getAfterProgressCompletionReplyMarkup(), InlineKeyboardMarkup.class));
            editMessageText.setMessageId(progress.getProgressMessageId());
            editMessageText.setParseMode(ParseMode.HTML);

            execute(editMessageText);
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e) {
            LOGGER.error("Error: " + e.getMessage() + " code: " + e.getErrorCode() + " description: " + e.getApiResponse(), e);
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String getLocalFilePath(String apiFilePath) {
        String path = apiFilePath.replace(localBotApiProperties.getWorkDir(), "");

        return localBotApiProperties.getLocalWorkDir() + path;
    }

    private interface Executable {

        void executeWithException() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }

    private interface Callable<V> {

        V executeWithResult() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }
}
