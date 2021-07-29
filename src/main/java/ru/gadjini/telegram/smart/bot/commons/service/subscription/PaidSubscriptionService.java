package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

public interface PaidSubscriptionService {

    PaidSubscription renewSubscription(String botName, long userId, int planId, Period period);

    PaidSubscriptionTariffType tariffType();
}
