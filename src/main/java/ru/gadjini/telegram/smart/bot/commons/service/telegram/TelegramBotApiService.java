package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadCanceledException;
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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("PMD")
public class TelegramBotApiService extends DefaultAbsSender implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiService.class);

    private final Map<String, SmartTempFile> downloading = new ConcurrentHashMap<>();

    private final Map<String, HttpPost> downloadingRequests = new ConcurrentHashMap<>();

    private final Map<String, SmartTempFile> uploading = new ConcurrentHashMap<>();

    private final BotProperties botProperties;

    private final BotApiProperties botApiProperties;

    private ObjectMapper objectMapper;

    private Method createHttpRequestMethod;

    private Method sendHttpPostRequestMethod;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties, ObjectMapper objectMapper,
                                 DefaultBotOptions botOptions, BotApiProperties botApiProperties) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
        this.botApiProperties = botApiProperties;
        try {
            this.createHttpRequestMethod = DefaultAbsSender.class.getDeclaredMethod("configuredHttpPost", String.class);
            this.createHttpRequestMethod.setAccessible(true);
            this.sendHttpPostRequestMethod = DefaultAbsSender.class.getDeclaredMethod("sendHttpPostRequest", HttpPost.class);
            this.sendHttpPostRequestMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
    public Message sendSticker(SendSticker sendSticker, Progress progress) {
        return uploadFile(sendSticker.getChatId(), () -> {
            Message execute = execute(sendSticker);

            return objectMapper.convertValue(execute, Message.class);
        }, sendSticker.getSticker(), progress);
    }

    @Override
    public Message sendDocument(SendDocument sendDocument, Progress progress) {
        return uploadFile(sendDocument.getChatId(), () -> {
            Message execute = execute(sendDocument);
            return objectMapper.convertValue(execute, Message.class);
        }, sendDocument.getDocument(), progress);
    }

    @Override
    public Message sendVideo(SendVideo sendVideo, Progress progress) {
        return uploadFile(sendVideo.getChatId(), () -> {
            Message execute = execute(sendVideo);
            return objectMapper.convertValue(execute, Message.class);
        }, sendVideo.getVideo(), progress);
    }

    @Override
    public Message sendAudio(SendAudio sendAudio, Progress progress) {
        return uploadFile(sendAudio.getChatId(), () -> {
            Message execute = execute(sendAudio);
            return objectMapper.convertValue(execute, Message.class);
        }, sendAudio.getAudio(), progress);
    }

    @Override
    public Message sendVoice(SendVoice sendVoice, Progress progress) {
        return uploadFile(sendVoice.getChatId(), () -> {
            Message execute = execute(sendVoice);
            return objectMapper.convertValue(execute, Message.class);
        }, sendVoice.getVoice(), progress);
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
                GetFile gf = new GetFile();
                gf.setFileId(fileId);
                HttpPost downloadingRequest = createDownloadingRequest(gf);
                downloadingRequests.put(fileId, downloadingRequest);
                try {
                    String responseContent = (String) sendHttpPostRequestMethod.invoke(this, downloadingRequest);
                    org.telegram.telegrambots.meta.api.objects.File file = gf.deserializeResponse(responseContent);
                    if (downloadingRequest.isAborted()) {
                        throw new DownloadCanceledException("Download canceled " + fileId);
                    }
                    String filePath = getLocalFilePath(file.getFilePath());

                    try {
                        FileUtils.copyFile(new File(filePath), outputFile.getFile());
                    } catch (IOException e) {
                        throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
                    } finally {
                        FileUtils.deleteQuietly(new File(filePath));
                    }
                } finally {
                    downloadingRequests.remove(fileId);
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
    public void cancelUploading(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        try {
            SmartTempFile tempFile = uploading.get(filePath);
            if (tempFile != null) {
                try {
                    tempFile.smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            uploading.remove(filePath);
        }
    }

    @Override
    public void cancelDownloading(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return;
        }
        try {
            SmartTempFile tempFile = downloading.get(fileId);
            if (tempFile != null) {
                try {
                    tempFile.smartDelete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            HttpPost httpPost = downloadingRequests.get(fileId);
            if (httpPost != null) {
                try {
                    httpPost.abort();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            downloading.remove(fileId);
            downloadingRequests.remove(fileId);
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
            for (Map.Entry<String, HttpPost> entry : downloadingRequests.entrySet()) {
                try {
                    entry.getValue().abort();
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
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    private <V> V uploadFile(String chatId, Callable<V> executable, InputFile inputFile, Progress progress) {
        if (inputFile.isNew()) {
            uploading.put(inputFile.getNewMediaFile().getAbsolutePath(), new SmartTempFile(inputFile.getNewMediaFile()));
        }

        try {
            updateProgressBeforeStart(progress);
            V result = executeWithResult(chatId, executable);
            updateProgressAfterComplete(progress);

            return result;
        } finally {
            if (inputFile.isNew()) {
                uploading.remove(inputFile.getNewMediaFile().getAbsolutePath());
            }
        }
    }

    private <V> V executeWithResult(String chatId, Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    private RuntimeException catchException(String chatId, Exception ex) {
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

    private HttpPost createDownloadingRequest(GetFile method) throws org.telegram.telegrambots.meta.exceptions.TelegramApiException {
        try {
            method.validate();
            String url = getBaseUrl() + method.getMethod();
            HttpPost httppost = (HttpPost) createHttpRequestMethod.invoke(this, url);
            httppost.addHeader("charset", StandardCharsets.UTF_8.name());
            httppost.setEntity(new StringEntity(objectMapper.writeValueAsString(method), ContentType.APPLICATION_JSON));

            return httppost;
        } catch (Exception e) {
            throw new org.telegram.telegrambots.meta.exceptions.TelegramApiException(e);
        }
    }

    private interface Executable {

        void executeWithException() throws Exception;
    }

    private interface Callable<V> {

        V executeWithResult() throws Exception;
    }
}
