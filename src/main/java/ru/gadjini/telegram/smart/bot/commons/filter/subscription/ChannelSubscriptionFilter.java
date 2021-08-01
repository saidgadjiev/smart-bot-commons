package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.filter.BaseBotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandsContainer;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.ChannelSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.utils.MessageUtils;

import java.util.Locale;

@Component
public class ChannelSubscriptionFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSubscriptionFilter.class);

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private ChannelSubscriptionService subscriptionService;

    private CommandParser commandParser;

    private CommandNavigator commandNavigator;

    private CommandsContainer commandsContainer;

    private SubscriptionProperties subscriptionProperties;

    private FixedTariffPaidSubscriptionService paidSubscriptionService;

    private BotProperties botProperties;

    @Autowired
    public ChannelSubscriptionFilter(@TgMessageLimitsControl MessageService messageService,
                                     LocalisationService localisationService, UserService userService,
                                     ChannelSubscriptionService subscriptionService, CommandParser commandParser,
                                     CommandNavigator commandNavigator, CommandsContainer commandsContainer,
                                     SubscriptionProperties subscriptionProperties, FixedTariffPaidSubscriptionService paidSubscriptionService, BotProperties botProperties) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.commandParser = commandParser;
        this.commandNavigator = commandNavigator;
        this.commandsContainer = commandsContainer;
        this.subscriptionProperties = subscriptionProperties;
        this.paidSubscriptionService = paidSubscriptionService;
        this.botProperties = botProperties;
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
        LOGGER.debug("Channel subscription required({})", user.getId());
        Locale locale = userService.getLocaleOrDefault(user.getId());
        String msg = localisationService.getMessage(MessagesProperties.MESSAGE_CHANNEL_SUBSCRIPTION_REQUIRED, locale);
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(user.getId())).text(msg)
                .parseMode(ParseMode.HTML).build());
    }

    private boolean isChannelSubscriptionRequiredForUpdate(Update update) {
        long userId = TgMessage.getUserId(update);
        PaidSubscription subscription = paidSubscriptionService.getSubscription(botProperties.getName(), userId);

        if (subscription != null && !subscription.isTrial()
                && (subscription.isActive() || subscription.isSubscriptionIntervalActive())) {
            return false;
        }
        if (update.hasMessage()) {
            String text = MessageUtils.getText(update.getMessage());
            if (update.getMessage().isCommand()) {
                String command = commandParser.parseBotCommand(update.getMessage()).getCommandName();
                BotCommand botCommand = commandsContainer.getBotCommand(command);

                if (botCommand == null) {
                    return false;
                }

                return botCommand.isChannelSubscriptionRequired();
            } else if (commandsContainer.isKeyboardCommand(update.getMessage().getChatId(), text)) {
                KeyboardBotCommand keyboardBotCommand = commandsContainer.getKeyboardBotCommand(update.getMessage().getChatId(), text);

                return keyboardBotCommand.isChannelSubscriptionRequired();
            } else {
                NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(update.getMessage().getChatId(), true);

                if (navigableBotCommand != null && navigableBotCommand.acceptNonCommandMessage(update.getMessage())) {
                    return navigableBotCommand.isChannelSubscriptionRequired(update.getMessage());
                }
            }
        }

        return false;
    }
}