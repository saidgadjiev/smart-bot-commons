package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

public interface PaidSubscriptionDao {

    void create(PaidSubscription paidSubscription);

    PaidSubscription getPaidSubscription(String botName, int userId);

}
