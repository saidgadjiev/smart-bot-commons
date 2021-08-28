package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.joda.time.Period;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class UzSubscriptionTimeDeclensionService implements SubscriptionTimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.UZ_LOCALE;
    }

    @Override
    public String localize(Period period) {
        StringBuilder result = new StringBuilder();
        if (period.getYears() != 0) {
            result.append(period.getMonths()).append(" yil");
        }
        if (period.getMonths() != 0) {
            result.append(period.getMonths()).append(" oy");
        }
        if (period.getDays() != 0) {
            result.append(period.getDays()).append(" kun");
        }

        return result.toString();
    }
}
