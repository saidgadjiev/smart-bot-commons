package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadCanceledException;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
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
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Progress;
import ru.gadjini.telegram.smart.bot.commons.model.web.HttpCodes;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
public class TelegramBotApiService extends DefaultAbsSender implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiService.class);

    private final Map<String, SmartTempFile> downloading = new ConcurrentHashMap<>();

    private final Map<String, Future<?>> downloadingFuture = new ConcurrentHashMap<>();

    private final Map<String, SmartTempFile> uploading = new ConcurrentHashMap<>();

    private final BotProperties botProperties;

    private final BotApiProperties localBotApiProperties;

    private ObjectMapper objectMapper;

    private SmartToTgModelMapper modelMapper;

    private ThreadPoolExecutor mediaWorkers;

    @Value("${file.downloading.concurrency.level:2}")
    private int fileDownloadingConcurrencyLevel;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties, ObjectMapper objectMapper,
                                 DefaultBotOptions botOptions, BotApiProperties localBotApiProperties,
                                 SmartToTgModelMapper modelMapper) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
        this.localBotApiProperties = localBotApiProperties;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("File downloading concurrency level({})", fileDownloadingConcurrencyLevel);
        this.mediaWorkers = mediaWorkers();
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
        executeWithoutResult(() -> {
            execute(objectMapper.convertValue(editMessageReplyMarkup, org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup.class));
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        executeWithoutResult(() -> {
            execute(objectMapper.convertValue(editMessageText, org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class));
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        executeWithoutResult(() -> {
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
        if (StringUtils.isNotBlank(sendDocument.getDocument().getFilePath())) {
            uploading.put(sendDocument.getDocument().getFilePath(), new SmartTempFile(new File(sendDocument.getDocument().getFilePath())));
        }

        try {
            return executeWithResult(() -> {
                updateProgressBeforeStart(sendDocument.getProgress());
                org.telegram.telegrambots.meta.api.objects.Message execute = execute(modelMapper.map(sendDocument));

                updateProgressAfterComplete(sendDocument.getProgress());

                return objectMapper.convertValue(execute, Message.class);
            });
        } finally {
            if (StringUtils.isNotBlank(sendDocument.getDocument().getFilePath())) {
                uploading.remove(sendDocument.getDocument().getFilePath());
            }
        }
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
        downloading.put(fileId, outputFile);
        Future<?> submit = mediaWorkers.submit(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                LOGGER.debug("Start downloadFileByFileId({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));

                executeWithoutResult(() -> {
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
            } catch (Exception e) {
                LOGGER.error(e.getMessage() + "({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
                throw new UnknownDownloadingUploadingException(e);
            }
        });

        try {
            downloadingFuture.put(fileId, submit);
            submit.get();

            downloadingFuture.remove(fileId);
            downloading.remove(fileId);
        } catch (InterruptedException e) {
            throw new DownloadCanceledException("Download canceled " + fileId);
        } catch (Exception e) {
            throw new DownloadingException(e.getMessage(), e);
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
            Future<?> future = downloadingFuture.get(fileId);
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }

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
            downloadingFuture.remove(fileId);
        }
    }

    @Override
    public void cancelDownloads() {
        try {
            for (Map.Entry<String, Future<?>> entry : downloadingFuture.entrySet()) {
                if (entry.getValue().isDone()) {
                    entry.getValue().cancel(true);
                }
            }
            for (Map.Entry<String, SmartTempFile> entry : downloading.entrySet()) {
                try {
                    entry.getValue().smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            downloading.clear();
            downloadingFuture.clear();
        }
        LOGGER.debug("Downloads canceled");
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private void executeWithoutResult(Executable executable) {
        try {
            executable.executeWithException();
        } catch (Exception e) {
            throw catchException(e);
        }
    }

    private <V> V executeWithResult(Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (Exception e) {
            throw catchException(e);
        }
    }

    private RuntimeException catchException(Exception ex) {
        if (ex instanceof org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) {
            org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e = (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) ex;
            LOGGER.error(e.getMessage() + "\n" + e.getErrorCode() + "\n" + e.getApiResponse(), e);
            if (e.getErrorCode() == HttpCodes.TOO_MANY_REQUESTS) {
                return new FloodWaitException(e.getApiResponse(), 30);
            }
            return new TelegramApiRequestException(e.getApiResponse());
        } else if (ex instanceof org.telegram.telegrambots.meta.exceptions.TelegramApiException) {
            LOGGER.error(ex.getMessage(), ex);
            return new TelegramApiException(ex);
        } else {
            LOGGER.error(ex.getMessage(), ex);
            return new UnknownDownloadingUploadingException(ex);
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

    private ThreadPoolExecutor mediaWorkers() {
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(fileDownloadingConcurrencyLevel, fileDownloadingConcurrencyLevel, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        LOGGER.debug("File downloading workers thread pool({})", taskExecutor.getCorePoolSize());

        return taskExecutor;
    }

    private interface Executable {

        void executeWithException() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }

    private interface Callable<V> {

        V executeWithResult() throws org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    }
}
