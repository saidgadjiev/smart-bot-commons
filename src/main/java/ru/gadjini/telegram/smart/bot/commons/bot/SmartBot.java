package ru.gadjini.telegram.smart.bot.commons.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.controller.BotController;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.filter.BotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.HtmlMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updates.Close;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.updates.LogOut;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.TgToSmartModelMapper;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
public class SmartBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotController.class);

    private BotProperties botProperties;

    private BotFilter botFilter;

    private MessageService messageService;

    private UserService userService;

    private TgToSmartModelMapper modelMapper;

    @Autowired
    public SmartBot(BotProperties botProperties, BotFilter botFilter,
                    @Qualifier("messageLimits") MessageService messageService,
                    UserService userService, TgToSmartModelMapper modelMapper,
                    DefaultBotOptions botOptions) {
        super(botOptions);
        this.botProperties = botProperties;
        this.botFilter = botFilter;
        this.messageService = messageService;
        this.userService = userService;
        this.modelMapper = modelMapper;
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
    public void onUpdateReceived(Update tgUpdate) {
        ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Update update = modelMapper.map(tgUpdate);
        try {
            botFilter.doFilter(update);
        } catch (UserException ex) {
            if (ex.isPrintLog()) {
                LOGGER.error(ex.getMessage(), ex);
            }
            messageService.sendMessage(new HtmlMessage(TgMessage.getChatId(update), ex.getHumanMessage()).setReplyToMessageId(ex.getReplyToMessageId()));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            TgMessage tgMessage = TgMessage.from(update);
            messageService.sendErrorMessage(tgMessage.getChatId(), userService.getLocaleOrDefault(tgMessage.getUser().getId()));
        }
    }
}
