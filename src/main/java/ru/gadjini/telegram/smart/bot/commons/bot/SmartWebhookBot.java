package ru.gadjini.telegram.smart.bot.commons.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.exception.ZeroLengthException;
import ru.gadjini.telegram.smart.bot.commons.filter.BotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
@Profile({SmartBotConfiguration.PROFILE_PROD, SmartBotConfiguration.PROFILE_LOAD_TEST})
@SuppressWarnings("PMD")
public class SmartWebhookBot extends SpringWebhookBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartWebhookBot.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public SmartWebhookBot(BotProperties botProperties, BotFilter botFilter,
                           @Qualifier("messageLimits") MessageService messageService,
                           UserService userService, DefaultBotOptions botOptions, SetWebhook setWebhook) {
        super(botOptions, setWebhook);
        this.botProperties = botProperties;
        this.botFilter = botFilter;
        this.messageService = messageService;
        this.userService = userService;

        LOGGER.debug("Webhook url({})", setWebhook.getUrl());
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
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
            if (!userService.handleBotBlockedByUser(ex)) {
                LOGGER.error(ex.getMessage(), ex);
                TgMessage tgMessage = TgMessage.from(update);
                messageService.sendErrorMessage(tgMessage.getChatId(), userService.getLocaleOrDefault(tgMessage.getUser().getId()));
            }
        }

        return null;
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
    public String getBotPath() {
        return botProperties.getName();
    }
}