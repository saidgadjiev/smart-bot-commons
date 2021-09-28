package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;

import java.util.Locale;

@Component
public class FixedTariffExpiredPaidSubscriptionHandler implements ExpiredPaidSubscriptionHandler {

    private MessageService messageService;

    private SmartInlineKeyboardService inlineKeyboardService;

    private SubscriptionProperties subscriptionProperties;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private UserService userService;

    private LocalisationService localisationService;

    @Autowired
    public FixedTariffExpiredPaidSubscriptionHandler(@TgMessageLimitsControl MessageService messageService,
                                                     SmartInlineKeyboardService inlineKeyboardService,
                                                     SubscriptionProperties subscriptionProperties,
                                                     PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                     UserService userService, LocalisationService localisationService) {
        this.messageService = messageService;
        this.inlineKeyboardService = inlineKeyboardService;
        this.subscriptionProperties = subscriptionProperties;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.userService = userService;
        this.localisationService = localisationService;
    }

    @Override
    public void handle(long userId, PaidSubscription paidSubscription) {
        sendSubscriptionRequired(userId);
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }

    public void sendSubscriptionRequired(long userId) {
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(getSubscriptionRequiredMessage(locale))
                        .replyMarkup(inlineKeyboardService.getPaymentKeyboard(subscriptionProperties.getPaymentBotName(), locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    private String getSubscriptionRequiredMessage(Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();
        return localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_REQUIRED, locale) + "\n\n"
                + localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES,
                new Object[]{NumberUtils.toString(minPrice, 2)},
                locale);
    }
}
