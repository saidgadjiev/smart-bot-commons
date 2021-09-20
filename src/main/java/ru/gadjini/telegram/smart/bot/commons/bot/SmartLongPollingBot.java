package ru.gadjini.telegram.smart.bot.commons.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApi;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.exception.InvalidMediaMessageException;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.filter.BotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserExceptionHandler;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
@Profile(Profiles.PROFILE_DEV_PRIMARY)
@SuppressWarnings({"PMD", "CPD-START"})
public class SmartLongPollingBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartLongPollingBot.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    private MessageService messageService;

    private UserService userService;

    private UserExceptionHandler userExceptionHandler;

    @Autowired
    public SmartLongPollingBot(BotProperties botProperties, BotFilter botFilter,
                               @TgMessageLimitsControl MessageService messageService,
                               UserService userService,
                               @TelegramBotApi DefaultBotOptions botOptions, UserExceptionHandler userExceptionHandler) {
        super(botOptions);
        this.botProperties = botProperties;
        this.botFilter = botFilter;
        this.messageService = messageService;
        this.userService = userService;
        this.userExceptionHandler = userExceptionHandler;

        LOGGER.debug("Long polling bot");
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
        } catch (InvalidMediaMessageException ignore) {

        } catch (UserException ex) {
            userExceptionHandler.handle(update, ex);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            TgMessage tgMessage = TgMessage.from(update);
            messageService.sendErrorMessage(tgMessage.getChatId(), userService.getLocaleOrDefault(tgMessage.getUser().getId()));
        }
    }
}
