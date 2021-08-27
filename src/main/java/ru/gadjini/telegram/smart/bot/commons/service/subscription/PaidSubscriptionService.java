package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

public interface PaidSubscriptionService {

    boolean isExpired(PaidSubscription paidSubscription);

    boolean isSubscriptionPeriodActive(PaidSubscription paidSubscription);

    PaidSubscription renewSubscription(long userId, int planId, Period period);

    PaidSubscriptionTariffType tariffType();
}
