package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

import java.time.LocalDate;

public interface PaidSubscriptionDao {

    void create(PaidSubscription paidSubscription);

    PaidSubscription getPaidSubscription(String botName, int userId);

    LocalDate updateEndDate(String botName, int userId, int planId, Period period);
}
