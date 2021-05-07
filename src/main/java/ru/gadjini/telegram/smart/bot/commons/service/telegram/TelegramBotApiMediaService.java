package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TelegramBotApiMediaService extends DefaultAbsSender implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiMediaService.class);

    private final BotProperties botProperties;

    private final BotApiProperties botApiProperties;

    private ObjectMapper objectMapper;

    private TelegramBotApiMethodExecutor exceptionHandler;

    public TelegramBotApiMediaService(BotProperties botProperties, ObjectMapper objectMapper,
                                      DefaultBotOptions botOptions, BotApiProperties botApiProperties,
                                      TelegramBotApiMethodExecutor exceptionHandler) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
        this.botApiProperties = botApiProperties;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        return exceptionHandler.executeWithResult(editMessageMedia.getChatId(), () -> {
            Message execute = (Message) execute(editMessageMedia);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public Message sendSticker(SendSticker sendSticker, Progress progress) {
        return uploadFile(sendSticker.getChatId(), () -> {
            Message execute = execute(sendSticker);

            return objectMapper.convertValue(execute, Message.class);
        }, progress);
    }

    @Override
    public Message sendDocument(SendDocument sendDocument, Progress progress) {
        return uploadFile(sendDocument.getChatId(), () -> {
            Message execute = execute(sendDocument);
            return objectMapper.convertValue(execute, Message.class);
        }, progress);
    }

    @Override
    public Message sendVideo(SendVideo sendVideo, Progress progress) {
        return uploadFile(sendVideo.getChatId(), () -> {
            Message execute = execute(sendVideo);
            return objectMapper.convertValue(execute, Message.class);
        }, progress);
    }

    @Override
    public Message sendAudio(SendAudio sendAudio, Progress progress) {
        return uploadFile(sendAudio.getChatId(), () -> {
            Message execute = execute(sendAudio);
            return objectMapper.convertValue(execute, Message.class);
        }, progress);
    }

    @Override
    public Message sendVoice(SendVoice sendVoice, Progress progress) {
        return uploadFile(sendVoice.getChatId(), () -> {
            Message execute = execute(sendVoice);
            return objectMapper.convertValue(execute, Message.class);
        }, progress);
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        return exceptionHandler.executeWithResult(sendPhoto.getChatId(), () -> {
            Message execute = execute(sendPhoto);

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        return downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.debug("Start downloadFileByFileId({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));

            AtomicLong resultFileSize = new AtomicLong();
            String resultFilePath = exceptionHandler.executeWithResult(fileId, () -> {
                updateProgressBeforeStart(progress);
                GetFile gf = new GetFile();
                gf.setFileId(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(gf);
                resultFileSize.set(file.getFileSize());
                String filePath = getLocalFilePath(file.getFilePath());

                if (outputFile != null) {
                    try {
                        Files.move(Path.of(filePath), outputFile.toPath(), REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
                    }

                    filePath = outputFile.getAbsolutePath();
                }

                updateProgressAfterComplete(progress);

                return filePath;
            });

            stopWatch.stop();
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {})", fileId,
                    MemoryUtils.humanReadableByteCount(resultFileSize.get()), stopWatch.getTime(TimeUnit.SECONDS));

            return resultFilePath;
        } catch (TelegramApiException | FloodWaitException e) {
            LOGGER.error(e.getMessage() + "({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw e;
        }
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    protected final String getLocalFilePath(String apiFilePath) {
        String path = apiFilePath.replace(botApiProperties.getWorkDir(), "");

        return botApiProperties.getLocalWorkDir() + path;
    }

    private <V> V uploadFile(String chatId, TelegramBotApiMethodExecutor.Callable<V> executable, Progress progress) {
        updateProgressBeforeStart(progress);

        return exceptionHandler.executeWithResult(chatId, executable);
    }

    @SuppressWarnings("PMD")
    final void updateProgressAfterComplete(Progress progress) {
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

    @SuppressWarnings("PMD")
    final void updateProgressBeforeStart(Progress progress) {
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
}
