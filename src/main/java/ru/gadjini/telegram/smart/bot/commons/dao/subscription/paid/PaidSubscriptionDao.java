package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

public interface PaidSubscriptionDao {

    void create(PaidSubscription paidSubscription);

    PaidSubscription getByBotNameAndUserId(String botName, int userId);

    void createOrRenew(PaidSubscription paidSubscription, Period period);

    int remove(String botName, int userId);

    default void refresh(String botName, int userId) {

    }

    default void refreshAll(String botName) {

    }
}
