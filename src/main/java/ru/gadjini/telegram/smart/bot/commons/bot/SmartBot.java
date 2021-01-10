package ru.gadjini.telegram.smart.bot.commons.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.Close;
import org.telegram.telegrambots.meta.api.methods.updates.LogOut;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.controller.BotController;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.exception.ZeroLengthException;
import ru.gadjini.telegram.smart.bot.commons.filter.BotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
public class SmartBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartBot.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public SmartBot(BotProperties botProperties, BotFilter botFilter,
                    @Qualifier("messageLimits") MessageService messageService,
                    UserService userService,
                    DefaultBotOptions botOptions) {
        super(botOptions);
        this.botProperties = botProperties;
        this.botFilter = botFilter;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void clearWebhook() {
        try {
            super.clearWebhook();
        } catch (TelegramApiException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (botProperties.isLogout()) {
            try {
                execute(new LogOut());
            } catch (TelegramApiException e) {
                throw new ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException(e);
            }
            throw new RuntimeException("Success logout");
        } else if (botProperties.isClose()) {
            try {
                execute(new Close());
            } catch (TelegramApiException e) {
                throw new ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException(e);
            }
            throw new RuntimeException("Success close");
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            botFilter.doFilter(update);
        } catch (ZeroLengthException ignore) {

        } catch (UserException ex) {
            if (ex.isPrintLog()) {
                LOGGER.error(ex.getMessage(), ex);
            }
            messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(TgMessage.getChatId(update)))
                    .parseMode(ParseMode.HTML)
                    .text(ex.getHumanMessage()).replyToMessageId(ex.getReplyToMessageId()).build());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            TgMessage tgMessage = TgMessage.from(update);
            messageService.sendErrorMessage(tgMessage.getChatId(), userService.getLocaleOrDefault(tgMessage.getUser().getId()));
        }
    }
}
