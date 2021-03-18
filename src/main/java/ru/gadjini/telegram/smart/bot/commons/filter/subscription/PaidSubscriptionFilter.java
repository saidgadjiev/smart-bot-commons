package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.filter.BaseBotFilter;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandsContainer;
import ru.gadjini.telegram.smart.bot.commons.service.declension.TimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.declension.TimeDeclensionService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;

@Component
public class PaidSubscriptionFilter extends BaseBotFilter {

    private SubscriptionProperties subscriptionProperties;

    private CommandParser commandParser;

    private CommandsContainer commandsContainer;

    private UserService userService;

    private LocalisationService localisationService;

    private MessageService messageService;

    private PaidSubscriptionService paidSubscriptionService;

    private SmartInlineKeyboardService inlineKeyboardService;

    private TimeDeclensionProvider timeDeclensionProvider;

    @Autowired
    public PaidSubscriptionFilter(SubscriptionProperties subscriptionProperties, CommandParser commandParser,
                                  CommandsContainer commandsContainer, @TgMessageLimitsControl MessageService messageService,
                                  LocalisationService localisationService, UserService userService,
                                  PaidSubscriptionService paidSubscriptionService, SmartInlineKeyboardService inlineKeyboardService,
                                  TimeDeclensionProvider timeDeclensionProvider) {
        this.subscriptionProperties = subscriptionProperties;
        this.commandParser = commandParser;
        this.commandsContainer = commandsContainer;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.paidSubscriptionService = paidSubscriptionService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.timeDeclensionProvider = timeDeclensionProvider;
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
        PaidSubscription subscription = paidSubscriptionService.getSubscription(user.getId());

        if (subscription == null) {
            paidSubscriptionService.createTrialSubscription(user.getId());
            sendTrialSubscriptionStarted(user);

            return false;
        }
        if (subscription.getEndDate().isBefore(LocalDate.now(ZoneOffset.UTC))) {
            sendSubscriptionExpired(user.getId());

            return false;
        }

        return true;
    }

    private void sendTrialSubscriptionStarted(User user) {
        Locale locale = userService.getLocaleOrDefault(user.getId());
        TimeDeclensionService declensionService = timeDeclensionProvider.getService(locale.getLanguage());

        int userId = user.getId();
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(
                                localisationService.getMessage(MessagesProperties.MESSAGE_TRIAL_PERIOD_STARTED,
                                        new Object[]{
                                                declensionService.day(subscriptionProperties.getTrialPeriod())
                                        }, locale))
                        .build()
        );
    }

    private void sendSubscriptionExpired(int userId) {
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED, locale))
                        .replyMarkup(inlineKeyboardService.getPaymentKeyboard(subscriptionProperties.getPaymentBotName()))
                        .build()
        );
    }

    private boolean isPaidSubscriptionRequiredForUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String command = commandParser.parseBotCommandName(update.getMessage());
            BotCommand botCommand = commandsContainer.getBotCommand(command);

            return botCommand.isPaidSubscriptionRequired();
        }

        return true;
    }
}
