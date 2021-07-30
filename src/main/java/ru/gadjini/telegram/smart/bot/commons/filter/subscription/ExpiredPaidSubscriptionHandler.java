package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

public interface ExpiredPaidSubscriptionHandler {

    void handle(long userId, PaidSubscription paidSubscription);

    PaidSubscriptionTariffType tariffType();
}
