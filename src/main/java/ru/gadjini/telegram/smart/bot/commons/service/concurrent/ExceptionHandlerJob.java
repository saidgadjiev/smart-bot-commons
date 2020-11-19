package ru.gadjini.telegram.smart.bot.commons.service.concurrent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.ProcessException;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class ExceptionHandlerJob implements SmartExecutorService.Job, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerJob.class);

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    private SmartExecutorService.Job job;

    ExceptionHandlerJob(MessageService messageService, UserService userService,
                        LocalisationService localisationService, SmartExecutorService.Job job) {
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
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
                    sendUserExceptionMessage(SendMessage.builder().chatId(String.valueOf(job.getChatId())).text(((UserException) e).getHumanMessage())
                            .parseMode(ParseMode.HTML)
                            .replyToMessageId(((UserException) e).getReplyToMessageId()).build());
                } else if (e instanceof ProcessException) {
                    LOGGER.error(e.getMessage(), e);
                    sendUserExceptionMessage(
                            SendMessage.builder().chatId(String.valueOf(job.getChatId()))
                                    .parseMode(ParseMode.HTML)
                                    .text(localisationService.getMessage(StringUtils.defaultIfBlank(job.getErrorCode(e),
                                            MessagesProperties.MESSAGE_ERROR), locale)).replyToMessageId(job.getReplyToMessageId()).build());
                } else if (floodWaitExceptionIndexOf != -1) {
                    LOGGER.error(e.getMessage());
                    FloodWaitException floodWaitException = (FloodWaitException) ExceptionUtils.getThrowableList(e).get(floodWaitExceptionIndexOf);
                    sendUserExceptionMessage(SendMessage.builder().chatId(String.valueOf(job.getChatId()))
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_IS_SLEEPING,
                                    new Object[]{floodWaitException.getSleepTime()},
                                    locale)
                            ).replyToMessageId(job.getReplyToMessageId())
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(floodWaitKeyboard(locale)).build());
                } else {
                    LOGGER.error(e.getMessage(), e);
                    sendUserExceptionMessage(SendMessage.builder().chatId(String.valueOf(job.getChatId()))
                            .text(localisationService.getMessage(StringUtils.defaultIfBlank(job.getErrorCode(e),
                                    MessagesProperties.MESSAGE_ERROR), locale))
                            .parseMode(ParseMode.HTML).replyToMessageId(job.getReplyToMessageId())
                            .build());
                }
            }
        }
    }

    private void sendUserExceptionMessage(SendMessage sendMessage) {
        if (job.isSuppressUserExceptions()) {
            return;
        }
        messageService.sendMessage(sendMessage);
    }

    public SmartExecutorService.Job getOriginalJob() {
        return job;
    }

    private InlineKeyboardMarkup floodWaitKeyboard(Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());
        inlineKeyboardMarkup.getKeyboard().add(List.of(retryButton(locale)));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton retryButton(Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.RETRY_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.RETRY + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add("a", job.getId())
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
