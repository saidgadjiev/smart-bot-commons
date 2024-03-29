package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.Locale;

public interface CheckPaidSubscriptionMessageBuilder {

    String getMessage(PaidSubscription paidSubscription, Locale locale);

    PaidSubscriptionTariffType tariffType();
}
