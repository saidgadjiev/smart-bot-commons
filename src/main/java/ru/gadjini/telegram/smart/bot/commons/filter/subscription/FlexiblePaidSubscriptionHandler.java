package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartInlineKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.util.Locale;

@Component
public class FlexiblePaidSubscriptionHandler implements ExpiredPaidSubscriptionHandler {

    private MessageService messageService;

    private FixedTariffExpiredPaidSubscriptionHandler fixedTariffExpiredPaidSubscriptionHandler;

    private LocalisationService localisationService;

    private UserService userService;

    private SmartInlineKeyboardService inlineKeyboardService;

    @Autowired
    public FlexiblePaidSubscriptionHandler(@TgMessageLimitsControl MessageService messageService,
                                           LocalisationService localisationService, UserService userService,
                                           SmartInlineKeyboardService inlineKeyboardService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.inlineKeyboardService = inlineKeyboardService;
    }

    @Autowired
    public void setFixedTariffExpiredPaidSubscriptionHandler(FixedTariffExpiredPaidSubscriptionHandler fixedTariffExpiredPaidSubscriptionHandler) {
        this.fixedTariffExpiredPaidSubscriptionHandler = fixedTariffExpiredPaidSubscriptionHandler;
    }

    @Override
    public void handle(long userId, PaidSubscription paidSubscription) {
        int daysLeft = JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval());
        if (daysLeft <= 0) {
            fixedTariffExpiredPaidSubscriptionHandler.sendSubscriptionRequired(userId);
        } else {
            Locale locale = userService.getLocaleOrDefault(userId);
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(userId))
                            .text(localisationService.getMessage(
                                    MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_REQUIRED, new Object[]{daysLeft},
                                    locale
                            ))
                            .replyMarkup(inlineKeyboardService.getFlexibleTariffSubscriptionRequiredKeyboard(locale))
                            .parseMode(ParseMode.HTML)
                            .build()
            );
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}
