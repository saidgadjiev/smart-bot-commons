package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

public interface PaidSubscriptionDao {

    PaidSubscription activateSubscriptionDay(long userId);

    void create(PaidSubscription paidSubscription);

    PaidSubscription getByBotNameAndUserId(long userId);

    void createOrRenew(PaidSubscription paidSubscription, Period period);

    int remove(long userId);

    default void refresh(long userId) {

    }

    default void refreshAll() {

    }
}
