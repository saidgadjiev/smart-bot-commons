package ru.gadjini.telegram.smart.bot.commons.service.declension;

public interface TimeDeclensionService {

    String getLocale();

    String day(int days);

    String months(int months);
}
