package ru.gadjini.telegram.smart.bot.commons.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart.SmartFileKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import java.util.Locale;
import java.util.Set;

@Service
public class FileUploadService {

    private UploadQueueService uploadQueueService;

    private UploadJob uploadJob;

    private WorkQueueDao workQueueDao;

    private SmartFileKeyboardService smartKeyboardService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    @Autowired
    public FileUploadService(UploadQueueService uploadQueueService,
                             WorkQueueDao workQueueDao, SmartFileKeyboardService smartKeyboardService,
                             @Qualifier("messageLimits") MessageService messageService,
                             LocalisationService localisationService, UserService userService) {
        this.uploadQueueService = uploadQueueService;
        this.workQueueDao = workQueueDao;
        this.smartKeyboardService = smartKeyboardService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Autowired
    public void setUploadJob(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
    }

    public void uploadSmartFile(int uploadId) {
        uploadQueueService.updateStatus(uploadId, QueueItem.Status.WAITING, QueueItem.Status.BLOCKED);
    }

    public void createUpload(int userId, String method, Object body, Progress progress, int producerId, Object extra) {
        if (isSmartFile()) {
            UploadQueueItem upload = uploadQueueService.createUpload(userId, method, body, progress, workQueueDao.getQueueName(),
                    workQueueDao.getProducerName(), producerId, QueueItem.Status.BLOCKED, extra);
            sendSmartFile(userId, upload.getId());
        } else {
            uploadQueueService.createUpload(userId, method, body, progress, workQueueDao.getQueueName(),
                    workQueueDao.getProducerName(), producerId, QueueItem.Status.WAITING, extra);
        }
    }

    public void createUpload(int userId, String method, Object body, Progress progress, int producerId) {
        createUpload(userId, method, body, progress, producerId, null);
    }

    public void cancelUploads(int producerId) {
        uploadJob.cancelUploads(workQueueDao.getQueueName(), producerId);
    }

    public void cancelUploads(Set<Integer> producerIds) {
        uploadJob.cancelUploads(workQueueDao.getQueueName(), producerIds);
    }

    public void cancelUploads() {
        uploadJob.cancelUploads();
    }

    private boolean isSmartFile() {
        return true;
    }

    private void sendSmartFile(int userId, int uploadId) {
        Locale locale = userService.getLocaleOrDefault(userId);
        InlineKeyboardMarkup smartKeyboard = smartKeyboardService.getSmartFileKeyboard(uploadId, locale);
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SMART_FILE_IS_READY, locale))
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(smartKeyboard)
                        .build()
        );
    }
}
