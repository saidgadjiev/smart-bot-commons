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
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandsContainer;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FixedTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.utils.MessageUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

@Component
public class PaidSubscriptionFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaidSubscriptionFilter.class);

    private SubscriptionProperties subscriptionProperties;

    private CommandParser commandParser;

    private CommandsContainer commandsContainer;

    private CommandNavigator commandNavigator;

    private UserService userService;

    private LocalisationService localisationService;

    private MessageService messageService;

    private FixedTariffPaidSubscriptionService paidSubscriptionService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private SmartInlineKeyboardService inlineKeyboardService;

    @Autowired
    public PaidSubscriptionFilter(SubscriptionProperties subscriptionProperties, CommandParser commandParser,
                                  CommandsContainer commandsContainer, CommandNavigator commandNavigator,
                                  @TgMessageLimitsControl MessageService messageService,
                                  LocalisationService localisationService, UserService userService,
                                  FixedTariffPaidSubscriptionService paidSubscriptionService,
                                  PaidSubscriptionPlanService paidSubscriptionPlanService,
                                  SmartInlineKeyboardService inlineKeyboardService) {
        this.subscriptionProperties = subscriptionProperties;
        this.commandParser = commandParser;
        this.commandsContainer = commandsContainer;
        this.commandNavigator = commandNavigator;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Override
    public void doFilter(Update update) {
        if (subscriptionProperties.isCheckPaidSubscription() && isPaidSubscriptionRequiredForUpdate(update)) {
            boolean subscriptionExists = checkSubscriptionOrStartTrial(TgMessage.getUser(update));

            if (subscriptionExists) {
                super.doFilter(update);
            }
        } else {
            super.doFilter(update);
        }
    }

    private boolean checkSubscriptionOrStartTrial(User user) {
        PaidSubscription subscription = paidSubscriptionService.getSubscription(subscriptionProperties.getPaidBotName(), user.getId());

        if (subscription == null) {
            LOGGER.debug("Trial subscription started({})", user.getId());
            PaidSubscription trialSubscription = paidSubscriptionService.createTrialSubscription(subscriptionProperties.getPaidBotName(), user.getId());
            sendTrialSubscriptionStarted(user, trialSubscription);

            return false;
        }
        if (!subscription.isActive()) {
            LOGGER.debug("Paid subscription required({})", user.getId());
            sendSubscriptionExpired(user.getId());

            return false;
        }

        return true;
    }

    private void sendTrialSubscriptionStarted(User user, PaidSubscription trialSubscription) {
        Locale locale = userService.getLocaleOrDefault(user.getId());
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        long userId = user.getId();
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(
                                localisationService.getMessage(MessagesProperties.MESSAGE_TRIAL_PERIOD_STARTED,
                                        new Object[]{
                                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(trialSubscription.getZonedEndDate()),
                                                TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                                                subscriptionProperties.getPaymentBotName(),
                                                NumberUtils.toString(minPrice, 2)
                                        }, locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    private void sendSubscriptionExpired(long userId) {
        Locale locale = userService.getLocaleOrDefault(userId);
        double minPrice = paidSubscriptionPlanService.getMinPrice();
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_REQUIRED,
                                new Object[] {NumberUtils.toString(minPrice, 2)}, locale))
                        .replyMarkup(inlineKeyboardService.getPaymentKeyboard(subscriptionProperties.getPaymentBotName(), locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    private boolean isPaidSubscriptionRequiredForUpdate(Update update) {
        if (update.hasMessage()) {
            String text = MessageUtils.getText(update.getMessage());
            if (commandsContainer.isBotCommand(update.getMessage())) {
                String command = commandParser.parseBotCommand(update.getMessage()).getCommandName();
                BotCommand botCommand = commandsContainer.getBotCommand(command);

                if (botCommand == null) {
                    return false;
                }

                return botCommand.isPaidSubscriptionRequired();
            } else if (commandsContainer.isKeyboardCommand(update.getMessage().getChatId(), text)) {
                KeyboardBotCommand keyboardBotCommand = commandsContainer.getKeyboardBotCommand(update.getMessage().getChatId(), text);

                return keyboardBotCommand.isPaidSubscriptionRequired();
            } else {
                NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(update.getMessage().getChatId(), true);

                if (navigableBotCommand != null && navigableBotCommand.acceptNonCommandMessage(update.getMessage())) {
                    return navigableBotCommand.isPaidSubscriptionRequired(update.getMessage());
                }
            }
        }

        return false;
    }
}
