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
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.IsChatMember;
import ru.gadjini.telegram.smart.bot.commons.model.web.HttpCodes;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("PMD")
public class TelegramBotApiService extends DefaultAbsSender implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiService.class);

    private final Map<String, SmartTempFile> downloading = new ConcurrentHashMap<>();

    private final Map<String, SmartTempFile> uploading = new ConcurrentHashMap<>();

    private final BotProperties botProperties;

    private final BotApiProperties botApiProperties;

    private ObjectMapper objectMapper;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties, ObjectMapper objectMapper,
                                 DefaultBotOptions botOptions, BotApiProperties botApiProperties) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
        this.botApiProperties = botApiProperties;
    }

    public Boolean isChatMember(IsChatMember isChatMember) {
        return executeWithResult(null, () -> {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(isChatMember.getChatId());
            getChatMember.setUserId(isChatMember.getUserId());

            ChatMember member = execute(getChatMember);

            return isInGroup(member.getStatus());
        });
    }

    public Boolean sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        return executeWithResult(null, () -> {
            return execute(objectMapper.convertValue(answerCallbackQuery, AnswerCallbackQuery.class));
        });
    }

    public Message sendMessage(SendMessage sendMessage) {
        return executeWithResult(sendMessage.getChatId(), () -> {
            Message execute = execute(objectMapper.convertValue(sendMessage, SendMessage.class));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    public void editReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        executeWithoutResult(editMessageReplyMarkup.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageReplyMarkup, EditMessageReplyMarkup.class));
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        executeWithoutResult(editMessageText.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageText, EditMessageText.class));
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        executeWithoutResult(editMessageCaption.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageCaption, EditMessageCaption.class));
        });
    }

    public Boolean deleteMessage(DeleteMessage deleteMessage) {
        return executeWithResult(deleteMessage.getChatId(), () -> {
            return execute(objectMapper.convertValue(deleteMessage, DeleteMessage.class));
        });
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        return executeWithResult(editMessageMedia.getChatId(), () -> {
            Message execute = (Message) execute(editMessageMedia);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendSticker(SendSticker sendSticker) {
        return executeWithResult(sendSticker.getChatId(), () -> {
            Message execute = execute(sendSticker);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendDocument(SendDocument sendDocument, Progress progress) {
        if (sendDocument.getDocument().isNew()) {
            uploading.put(sendDocument.getDocument().getNewMediaFile().getAbsolutePath(), new SmartTempFile(sendDocument.getDocument().getNewMediaFile()));
        }

        try {
            return executeWithResult(sendDocument.getChatId(), () -> {
                updateProgressBeforeStart(progress);
                Message execute = execute(sendDocument);

                updateProgressAfterComplete(progress);

                return objectMapper.convertValue(execute, Message.class);
            });
        } finally {
            if (sendDocument.getDocument().isNew()) {
                uploading.remove(sendDocument.getDocument().getNewMediaFile().getAbsolutePath());
            }
        }
    }

    @Override
    public Message sendVideo(SendVideo sendVideo) {
        return executeWithResult(sendVideo.getChatId(), () -> {
            Message execute = execute(sendVideo);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendAudio(SendAudio sendAudio) {
        return executeWithResult(sendAudio.getChatId(), () -> {
            Message execute = execute(sendAudio);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        return executeWithResult(sendPhoto.getChatId(), () -> {
            Message execute = execute(sendPhoto);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        downloading.put(fileId, outputFile);
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.debug("Start downloadFileByFileId({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));

            executeWithoutResult(null, () -> {
                updateProgressBeforeStart(progress);
                org.telegram.telegrambots.meta.api.methods.GetFile gf = new org.telegram.telegrambots.meta.api.methods.GetFile();
                gf.setFileId(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(gf);
                String filePath = getLocalFilePath(file.getFilePath());

                try {
                    FileUtils.copyFile(new File(filePath), outputFile.getFile());
                } catch (IOException e) {
                    throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
                } finally {
                    FileUtils.deleteQuietly(new File(filePath));
                }
                updateProgressAfterComplete(progress);
            });

            stopWatch.stop();
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {})", fileId, MemoryUtils.humanReadableByteCount(outputFile.length()), stopWatch.getTime(TimeUnit.SECONDS));
        } catch (TelegramApiException | FloodWaitException e) {
            LOGGER.error(e.getMessage() + "({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw e;
        } finally {
            downloading.remove(fileId);
        }
    }

    @Override
    public boolean cancelUploading(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        try {
            SmartTempFile tempFile = uploading.get(filePath);
            if (tempFile != null) {
                try {
                    tempFile.smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

                return true;
            }

            return false;
        } finally {
            uploading.remove(filePath);
        }
    }

    @Override
    public boolean cancelDownloading(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return false;
        }
        try {
            SmartTempFile tempFile = downloading.get(fileId);
            if (tempFile != null) {
                try {
                    tempFile.smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

                return true;
            }

            return false;
        } finally {
            downloading.remove(fileId);
        }
    }

    @Override
    public void cancelDownloads() {
        try {
            for (Map.Entry<String, SmartTempFile> entry : downloading.entrySet()) {
                try {
                    entry.getValue().smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            downloading.clear();
        }
        LOGGER.debug("Downloads canceled");
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
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
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(progress.getChatId());
            editMessageText.setText(progress.getProgressMessage());
            editMessageText.setReplyMarkup(objectMapper.convertValue(progress.getProgressReplyMarkup(), InlineKeyboardMarkup.class));
            editMessageText.setMessageId(progress.getProgressMessageId());
            editMessageText.setParseMode(ParseMode.HTML);

            execute(editMessageText);
        } catch (Exception ignore) {
        }
    }

    private void updateProgressAfterComplete(Progress progress) {
        if (progress == null || StringUtils.isBlank(progress.getAfterProgressCompletionMessage())) {
            return;
        }
        try {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(progress.getChatId());
            editMessageText.setText(progress.getAfterProgressCompletionMessage());
            editMessageText.setReplyMarkup(objectMapper.convertValue(progress.getAfterProgressCompletionReplyMarkup(), InlineKeyboardMarkup.class));
            editMessageText.setMessageId(progress.getProgressMessageId());
            editMessageText.setParseMode(ParseMode.HTML);

            execute(editMessageText);
        } catch (Exception ignore) {

        }
    }

    private String getLocalFilePath(String apiFilePath) {
        String path = apiFilePath.replace(botApiProperties.getWorkDir(), "");

        return botApiProperties.getLocalWorkDir() + path;
    }

    private void executeWithoutResult(String chatId, Executable executable) {
        try {
            executable.executeWithException();
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            throw catchException(chatId, e);
        }
    }

    private <V> V executeWithResult(String chatId, Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            throw catchException(chatId, e);
        }
    }

    private RuntimeException catchException(String chatId, org.telegram.telegrambots.meta.exceptions.TelegramApiException ex) {
        if (ex instanceof org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) {
            org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e = (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) ex;
            LOGGER.error(e.getMessage() + "\n" + e.getErrorCode() + "\n" + e.getApiResponse(), e);
            if (e.getErrorCode() == HttpCodes.TOO_MANY_REQUESTS) {
                return new FloodWaitException(e.getApiResponse(), 30);
            }
            return new TelegramApiRequestException(chatId, e.getMessage(), e.getErrorCode(), e.getApiResponse(), e);
        } else {
            LOGGER.error(ex.getMessage(), ex);
            return new TelegramApiException(ex);
        }
    }

    private interface Executable {

        void executeWithException() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }

    private interface Callable<V> {

        V executeWithResult() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }
}
