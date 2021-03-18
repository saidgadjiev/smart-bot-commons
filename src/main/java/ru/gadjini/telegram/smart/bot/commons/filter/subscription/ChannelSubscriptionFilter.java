package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.filter.BaseBotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.ChannelSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandsContainer;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

@Component
public class ChannelSubscriptionFilter extends BaseBotFilter {

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private ChannelSubscriptionService subscriptionService;

    private CommandParser commandParser;

    private CommandsContainer commandsContainer;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public ChannelSubscriptionFilter(@TgMessageLimitsControl MessageService messageService,
                                     LocalisationService localisationService, UserService userService,
                                     ChannelSubscriptionService subscriptionService, CommandParser commandParser,
                                     CommandsContainer commandsContainer, SubscriptionProperties subscriptionProperties) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.commandParser = commandParser;
        this.commandsContainer = commandsContainer;
        this.subscriptionProperties = subscriptionProperties;
    }

    @Override
    public void doFilter(Update update) {
        if (subscriptionProperties.isCheckChannelSubscription() && isChannelSubscriptionRequiredForUpdate(update)) {
            if (subscriptionService.isSubscriptionExists(TgMessage.getUserId(update))) {
                super.doFilter(update);
            } else {
                sendNeedSubscription(TgMessage.getUser(update));
            }
        } else {
            super.doFilter(update);
        }
    }

    private void sendNeedSubscription(User user) {
        Locale locale = userService.getLocaleOrDefault(user.getId());
        String msg = localisationService.getMessage(MessagesProperties.MESSAGE_CHANNEL_SUBSCRIPTION_REQUIRED, locale);
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(user.getId())).text(msg)
                .parseMode(ParseMode.HTML).build());
    }

    private boolean isChannelSubscriptionRequiredForUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String command = commandParser.parseBotCommandName(update.getMessage());
            BotCommand botCommand = commandsContainer.getBotCommand(command);

            return botCommand.isChannelSubscriptionRequired();
        }

        return true;
    }
}