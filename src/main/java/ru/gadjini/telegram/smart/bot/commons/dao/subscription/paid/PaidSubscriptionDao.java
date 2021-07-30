package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

public interface PaidSubscriptionDao {

    PaidSubscription activateSubscriptionDay(String botName, long userId);

    void create(PaidSubscription paidSubscription);

    PaidSubscription getByBotNameAndUserId(String botName, long userId);

    void createOrRenew(PaidSubscription paidSubscription, Period period);

    int remove(String botName, long userId);

    default void refresh(String botName, long userId) {

    }

    default void refreshAll(String botName) {

    }
}
