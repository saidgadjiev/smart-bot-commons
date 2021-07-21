package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.joda.time.Period;

public interface SubscriptionTimeDeclensionService {

    String getLocale();

    String localize(Period period);
}
