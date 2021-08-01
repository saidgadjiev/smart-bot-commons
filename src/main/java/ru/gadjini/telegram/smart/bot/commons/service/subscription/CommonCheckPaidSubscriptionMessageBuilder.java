package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

import java.util.Locale;

public interface CommonCheckPaidSubscriptionMessageBuilder {

    String getMessage(PaidSubscription paidSubscription, Locale locale);
}
