package ru.gadjini.telegram.smart.bot.commons.service.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.HtmlMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.SendMessage;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;
import java.util.function.Supplier;

public class ExceptionHandlerJob implements SmartExecutorService.Job, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerJob.class);

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    private FileManager fileManager;

    private SmartExecutorService.Job job;

    ExceptionHandlerJob(MessageService messageService, UserService userService,
                        LocalisationService localisationService,
                        FileManager fileManager, SmartExecutorService.Job job) {
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
        this.fileManager = fileManager;
        this.job = job;
    }

    @Override
    public void execute() throws Exception {
        job.execute();
    }

    @Override
    public int getId() {
        return job.getId();
    }

    @Override
    public SmartExecutorService.JobWeight getWeight() {
        return job.getWeight();
    }

    @Override
    public long getChatId() {
        return job.getChatId();
    }

    @Override
    public int getProgressMessageId() {
        return job.getProgressMessageId();
    }

    @Override
    public String getErrorCode(Throwable e) {
        return job.getErrorCode(e);
    }

    @Override
    public void cancel() {
        job.cancel();
    }

    @Override
    public void setCancelChecker(Supplier<Boolean> checker) {
        job.setCancelChecker(checker);
    }

    @Override
    public void setCanceledByUser(boolean canceledByUser) {
        job.setCanceledByUser(canceledByUser);
    }

    @Override
    public void run() {
        try {
            job.execute();
        } catch (Throwable e) {
            if (userService.handleBotBlockedByUser(e)) {
                TelegramApiRequestException exception = (TelegramApiRequestException) e;
                LOGGER.error("Bot is blocked({})", exception.getChatId());
                return;
            }
            if (!job.getCancelChecker().get()) {
                Locale locale = userService.getLocaleOrDefault((int) job.getChatId());

                int floodWaitExceptionIndexOf = ExceptionUtils.indexOfThrowable(e, FloodWaitException.class);
                if (e instanceof UserException) {
                    if (((UserException) e).isPrintLog()) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    sendUserExceptionMessage(
                            new HtmlMessage(job.getChatId(), ((UserException) e).getHumanMessage())
                                    .setReplyToMessageId(((UserException) e).getReplyToMessageId()));
                } else if (e instanceof ProcessException) {
                    LOGGER.error(e.getMessage(), e);
                    sendUserExceptionMessage(
                            new HtmlMessage(job.getChatId(),
                                    localisationService.getMessage(StringUtils.defaultIfBlank(job.getErrorCode(e),
                                            MessagesProperties.MESSAGE_ERROR), locale)).setReplyToMessageId(job.getReplyToMessageId()));
                } else if (floodWaitExceptionIndexOf != -1) {
                    LOGGER.error(e.getMessage());
                    fileManager.resetLimits(job.getChatId());
                    FloodWaitException floodWaitException = (FloodWaitException) ExceptionUtils.getThrowableList(e).get(floodWaitExceptionIndexOf);
                    sendUserExceptionMessage(new HtmlMessage(job.getChatId(),
                            localisationService.getMessage(MessagesProperties.MESSAGE_BOT_IS_SLEEPING,
                                    new Object[]{floodWaitException.getSleepTime()},
                                    locale)
                    ).setReplyToMessageId(job.getReplyToMessageId()));
                } else {
                    LOGGER.error(e.getMessage(), e);
                    sendUserExceptionMessage(new HtmlMessage(job.getChatId(),
                            localisationService.getMessage(StringUtils.defaultIfBlank(job.getErrorCode(e),
                                    MessagesProperties.MESSAGE_ERROR), locale)).setReplyToMessageId(job.getReplyToMessageId()));
                }
            }
        }
    }

    private void sendUserExceptionMessage(SendMessage sendMessage) {
        if (job.isSuppressUserExceptions()) {
            return;
        }
        try {
            messageService.sendMessage(sendMessage);
        } catch (TelegramApiRequestException ex) {
            if (ex.getErrorCode() == 400 && ex.getMessage().contains("reply message not found")) {
                LOGGER.debug("Reply message not found try send without reply");
                sendMessage.setReplyToMessageId(null);
                messageService.sendMessage(sendMessage);
            }
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public SmartExecutorService.Job getOriginalJob() {
        return job;
    }
}
