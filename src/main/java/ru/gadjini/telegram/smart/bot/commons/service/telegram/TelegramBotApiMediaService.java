package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiConstants;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.NetSpeedUtils;

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

    private TelegramBotApiMethodExecutor exceptionHandler;

    public TelegramBotApiMediaService(BotProperties botProperties,
                                      DefaultBotOptions botOptions, BotApiProperties botApiProperties,
                                      TelegramBotApiMethodExecutor exceptionHandler) {
        super(botOptions);
        this.botProperties = botProperties;
        this.botApiProperties = botApiProperties;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Message sendAnimation(SendAnimation sendAnimation, Progress progress) {
        return uploadFile(sendAnimation.getChatId(), () -> execute(sendAnimation), progress);
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        return exceptionHandler.executeWithResult(editMessageMedia.getChatId(), () -> (Message) execute(editMessageMedia));
    }

    @Override
    public Message sendSticker(SendSticker sendSticker, Progress progress) {
        return uploadFile(sendSticker.getChatId(), () -> execute(sendSticker), progress);
    }

    @Override
    public Message sendDocument(SendDocument sendDocument, Progress progress) {
        return uploadFile(sendDocument.getChatId(), () -> execute(sendDocument), progress);
    }

    @Override
    public Message sendVideo(SendVideo sendVideo, Progress progress) {
        return uploadFile(sendVideo.getChatId(), () -> execute(sendVideo), progress);
    }

    @Override
    public Message sendVideoNote(SendVideoNote sendVideoNote, Progress progress) {
        return uploadFile(sendVideoNote.getChatId(), () -> execute(sendVideoNote), progress);
    }

    @Override
    public Message sendAudio(SendAudio sendAudio, Progress progress) {
        return uploadFile(sendAudio.getChatId(), () -> execute(sendAudio), progress);
    }

    @Override
    public Message sendVoice(SendVoice sendVoice, Progress progress) {
        return uploadFile(sendVoice.getChatId(), () -> execute(sendVoice), progress);
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        return exceptionHandler.executeWithResult(sendPhoto.getChatId(), () -> execute(sendPhoto));
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        return downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    @Override
    public String downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        AtomicLong resultFileSize = new AtomicLong(fileSize);
        try {
            LOGGER.debug("Start downloadFileByFileId({}, {}, {})", isLocal(), fileId, MemoryUtils.humanReadableByteCount(fileSize));

            return exceptionHandler.executeWithResult(fileId, () -> {
                updateProgressBeforeStart(progress);
                GetFile gf = new GetFile();
                gf.setFileId(fileId);
                org.telegram.telegrambots.meta.api.objects.File file = execute(gf);
                resultFileSize.set(file.getFileSize());
                String filePath;
                if (!isLocal()) {
                    filePath = getLocalFilePath(file.getFilePath());

                    if (outputFile != null) {
                        try {
                            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                                LOGGER.debug("Error mkdirs({}, {})", outputFile.getParentFile().getAbsolutePath(), fileId);
                            }
                            Files.move(Path.of(filePath), outputFile.toPath(), REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        filePath = outputFile.getAbsolutePath();
                    } else {
                        LOGGER.debug("Directly downloaded file may be deleted!({}, {})", filePath, fileId);
                    }
                } else {
                    downloadFile(file, outputFile.getFile());
                    filePath = outputFile.getAbsolutePath();
                }

                updateProgressAfterComplete(progress);

                LOGGER.debug("Finished successfully downloadFileByFileId({}, {}, {})", isLocal(), fileId,
                        MemoryUtils.humanReadableByteCount(resultFileSize.get()));

                return filePath;
            });
        } catch (TelegramApiException | FloodWaitException e) {
            LOGGER.error(e.getMessage() + "({}, {}, {})", isLocal(), fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw e;
        } finally {
            stopWatch.stop();
            long time = stopWatch.getTime(TimeUnit.SECONDS);
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {}, {}, {})", isLocal(), fileId,
                    MemoryUtils.humanReadableByteCount(resultFileSize.get()), time,
                    NetSpeedUtils.toSpeed(resultFileSize.get() / Math.max(1, time)));
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

        V result = exceptionHandler.executeWithResult(chatId, executable);

        updateProgressAfterComplete(progress);

        return result;
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
            editMessageText.setReplyMarkup(progress.getAfterProgressCompletionReplyMarkup());
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
            editMessageText.setReplyMarkup(progress.getProgressReplyMarkup());
            editMessageText.setMessageId(progress.getProgressMessageId());
            editMessageText.setParseMode(ParseMode.HTML);

            execute(editMessageText);
        } catch (Exception ignore) {
        }
    }

    final boolean isLocal() {
        return getOptions().getBaseUrl().equals(ApiConstants.BASE_URL);
    }
}
