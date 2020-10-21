package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadCanceledException;
import ru.gadjini.telegram.smart.bot.commons.exception.DownloadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.UnknownDownloadingUploadingException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.ApiResponse;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.MediaType;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.CancelDownloading;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.CancelUploading;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.*;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updatemessages.EditMessageMedia;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.GetFile;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.MTProtoProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Service
@SuppressWarnings("CPD-START")
public class TelegramMTProtoService implements TelegramMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramMTProtoService.class);

    private final Map<String, SmartTempFile> downloading = new ConcurrentHashMap<>();

    private final Map<String, Future<?>> downloadingFuture = new ConcurrentHashMap<>();

    private final Map<String, SmartTempFile> uploading = new ConcurrentHashMap<>();

    private final MTProtoProperties telegramProperties;

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    private ThreadPoolExecutor mediaWorkers;

    @Value("${mtproto.concurrency.level:2}")
    private int concurrencyLevel;

    @Autowired
    public TelegramMTProtoService(MTProtoProperties telegramProperties, ObjectMapper objectMapper) {
        this.telegramProperties = telegramProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        LOGGER.debug("MTProto: " + telegramProperties.getApi());
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("MTproto concurrency level({})", concurrencyLevel);
        this.mediaWorkers = mediaWorkers();
    }

    @Override
    public Message editMessageMedia(EditMessageMedia editMessageMedia) {
        try {
            HttpEntity<EditMessageMedia> request = new HttpEntity<>(editMessageMedia);
            String response = restTemplate.postForObject(getUrl(EditMessageMedia.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(editMessageMedia.getChatId(), "Error editing message media", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(editMessageMedia.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiRequestException(editMessageMedia.getChatId(), e.getMessage(), e);
        }
    }

    @Override
    public Message sendSticker(SendSticker sendSticker) {
        try {
            HttpEntity<SendSticker> request = new HttpEntity<>(sendSticker);
            String response = restTemplate.postForObject(getUrl(SendSticker.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(sendSticker.getChatId(), "Error sending sticker", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(sendSticker.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiRequestException(sendSticker.getChatId(), e.getMessage(), e);
        }
    }

    @Override
    public Message sendDocument(SendDocument sendDocument) {
        if (StringUtils.isNotBlank(sendDocument.getDocument().getFilePath())) {
            uploading.put(sendDocument.getDocument().getFilePath(), new SmartTempFile(new File(sendDocument.getDocument().getFilePath())));
        }
        try {
            HttpEntity<SendDocument> request = new HttpEntity<>(sendDocument);
            String response = restTemplate.postForObject(getUrl(SendDocument.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(sendDocument.getChatId(), "Error sending document", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(sendDocument.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (TelegramApiRequestException ex) {
            throw ex;
        } catch (Exception e) {
            throw new UnknownDownloadingUploadingException(e);
        } finally {
            if (StringUtils.isNotBlank(sendDocument.getDocument().getFilePath())) {
                uploading.remove(sendDocument.getDocument().getFilePath());
            }
        }
    }

    @Override
    public Message sendVideo(SendVideo sendVideo) {
        try {
            HttpEntity<SendVideo> request = new HttpEntity<>(sendVideo);
            String response = restTemplate.postForObject(getUrl(SendVideo.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(sendVideo.getChatId(), "Error sending document", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(sendVideo.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiRequestException(sendVideo.getChatId(), e.getMessage(), e);
        }
    }

    @Override
    public Message sendAudio(SendAudio sendAudio) {
        try {
            HttpEntity<SendAudio> request = new HttpEntity<>(sendAudio);
            String response = restTemplate.postForObject(getUrl(SendAudio.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(sendAudio.getChatId(), "Error sending document", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(sendAudio.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiRequestException(sendAudio.getChatId(), e.getMessage(), e);
        }
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        try {
            HttpEntity<SendPhoto> request = new HttpEntity<>(sendPhoto);
            String response = restTemplate.postForObject(getUrl(SendPhoto.METHOD), request, String.class);
            try {
                ApiResponse<Message> result = objectMapper.readValue(response, new TypeReference<>() {
                });
                if (result.getOk()) {
                    return result.getResult();
                } else {
                    throw new TelegramApiRequestException(sendPhoto.getChatId(), "Error sending photo", result);
                }
            } catch (IOException e) {
                throw new TelegramApiRequestException(sendPhoto.getChatId(), "Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiRequestException(sendPhoto.getChatId(), e.getMessage(), e);
        }
    }

    public MediaType getMediaType(String fileId) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("file_id", fileId);
            HttpEntity<ObjectNode> request = new HttpEntity<>(objectNode);
            String response = restTemplate.postForObject(getUrl("head"), request, String.class);
            try {
                ObjectNode result = objectMapper.readValue(response, new TypeReference<>() {
                });
                JsonNode jsonNode = result.get("media_type");

                if (jsonNode.isNull()) {
                    LOGGER.debug("Media type not resolved({})", fileId);

                    return MediaType.DOCUMENT;
                } else {
                    int mediaType = jsonNode.asInt();
                    return MediaType.fromCode(mediaType);
                }
            } catch (IOException e) {
                throw new TelegramApiException(fileId + " Unable to deserialize response(" + response + ")\n" + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            throw new TelegramApiException(fileId + " " + e.getMessage(), e);
        }
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, SmartTempFile outputFile) {
        downloadFileByFileId(fileId, fileSize, null, outputFile);
    }

    public void downloadFileOverBackupChannel(String fileId, long fileSize, SmartTempFile outputFile) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.debug("Start downloadFileByFileId({})", fileId);

            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            getFile.setFileSize(fileSize);
            getFile.setPath(outputFile.getAbsolutePath());
            getFile.setRemoveParentDirOnCancel(false);
            HttpEntity<GetFile> request = new HttpEntity<>(getFile);
            String result = restTemplate.postForObject(getUrl(GetFile.METHOD), request, String.class);
            try {
                ApiResponse<Void> apiResponse = objectMapper.readValue(result, new TypeReference<>() {
                });

                if (!apiResponse.getOk()) {
                    if (Objects.equals(apiResponse.getErrorCode(), 420)) {
                        throw new FloodWaitException("Flood wait", (int) Double.parseDouble(apiResponse.getErrorDescription()));
                    }
                    throw new DownloadCanceledException("Download canceled " + fileId);
                }
            } catch (IOException e) {
                throw new TelegramApiException("Unable to deserialize response(" + result + ", " + fileId + ")\n" + e.getMessage(), e);
            }

            stopWatch.stop();
            LOGGER.debug("Finish downloadFileByFileId({}, {}, {})", fileId, MemoryUtils.humanReadableByteCount(outputFile.length()), stopWatch.getTime(TimeUnit.SECONDS));
        } catch (DownloadCanceledException | FloodWaitException | TelegramApiException e) {
            LOGGER.error("Download canceled({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error download({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
            throw new UnknownDownloadingUploadingException(e);
        }
    }

    @Override
    public void downloadFileByFileId(String fileId, long fileSize, Progress progress, SmartTempFile outputFile) {
        downloading.put(fileId, outputFile);

        Future<?> submit = mediaWorkers.submit(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                LOGGER.debug("Start downloadFileByFileId({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));

                GetFile getFile = new GetFile();
                getFile.setFileId(fileId);
                getFile.setFileSize(fileSize);
                getFile.setPath(outputFile.getAbsolutePath());
                getFile.setRemoveParentDirOnCancel(false);
                getFile.setProgress(progress);
                HttpEntity<GetFile> request = new HttpEntity<>(getFile);
                String result = restTemplate.postForObject(getUrl(GetFile.METHOD), request, String.class);
                try {
                    ApiResponse<Void> apiResponse = objectMapper.readValue(result, new TypeReference<>() {
                    });

                    if (!apiResponse.getOk()) {
                        if (Objects.equals(apiResponse.getErrorCode(), 420)) {
                            throw new FloodWaitException("Flood wait", (int) Double.parseDouble(apiResponse.getErrorDescription()));
                        }
                        throw new DownloadCanceledException("Download canceled " + fileId);
                    }
                } catch (IOException e) {
                    throw new TelegramApiException("Unable to deserialize response(" + result + ", " + fileId + ")\n" + e.getMessage(), e);
                }

                stopWatch.stop();
                LOGGER.debug("Finish downloadFileByFileId({}, {}, {})", fileId, MemoryUtils.humanReadableByteCount(outputFile.length()), stopWatch.getTime(TimeUnit.SECONDS));
            } catch (DownloadCanceledException | FloodWaitException | TelegramApiException e) {
                LOGGER.error(e.getMessage() + "({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
                throw e;
            } catch (Exception e) {
                LOGGER.error("Error download({}, {})", fileId, MemoryUtils.humanReadableByteCount(fileSize));
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
            cancelDownloading(fileId);

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
                    HttpEntity<CancelUploading> request = new HttpEntity<>(new CancelUploading(filePath));
                    restTemplate.postForObject(getUrl(CancelUploading.METHOD), request, Void.class);
                } finally {
                    try {
                        tempFile.smartDelete();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
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
                    HttpEntity<CancelDownloading> request = new HttpEntity<>(new CancelDownloading(fileId));
                    restTemplate.postForObject(getUrl(CancelDownloading.METHOD), request, Void.class);
                } finally {
                    try {
                        tempFile.smartDelete();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }

                return true;
            }
            Future<?> future = downloadingFuture.get(fileId);
            if (future != null && !future.isDone()) {
                future.cancel(true);
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
            for (Map.Entry<String, SmartTempFile> entry : downloading.entrySet()) {
                try {
                    HttpEntity<CancelDownloading> request = new HttpEntity<>(new CancelDownloading(entry.getKey()));
                    restTemplate.postForObject(getUrl(CancelDownloading.METHOD), request, Void.class);
                } finally {
                    try {
                        entry.getValue().smartDelete();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
            for (Map.Entry<String, Future<?>> entry : downloadingFuture.entrySet()) {
                if (entry.getValue().isDone()) {
                    entry.getValue().cancel(true);
                }
            }
        } finally {
            downloading.clear();
            downloadingFuture.clear();
        }
        LOGGER.debug("Downloads canceled");
    }

    public void restoreFileIfNeed(String filePath, String fileId) {
        if (!new File(filePath).exists()) {
            downloadFileByFileId(fileId, 0, new SmartTempFile(new File(filePath)));
            LOGGER.debug("File restored({}, {})", fileId, filePath);
        }
    }

    private ThreadPoolExecutor mediaWorkers() {
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(concurrencyLevel, concurrencyLevel, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        LOGGER.debug("Media workers thread pool({})", taskExecutor.getCorePoolSize());

        return taskExecutor;
    }

    private String getUrl(String method) {
        return telegramProperties.getApi() + method;
    }
}
