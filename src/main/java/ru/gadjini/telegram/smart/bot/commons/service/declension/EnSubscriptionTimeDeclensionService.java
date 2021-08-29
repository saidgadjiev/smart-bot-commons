package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.joda.time.Period;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class EnSubscriptionTimeDeclensionService implements SubscriptionTimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.EN_LOCALE;
    }

    @Override
    public String localize(Period period) {
        StringBuilder result = new StringBuilder();

        if (period.getYears() == 1) {
            result.append("1 year");
        } else if (period.getYears() != 0) {
            result.append(period.getYears()).append(" years");
        }
        if (period.getMonths() == 1) {
            result.append("1 month");
        } else if (period.getMonths() > 0) {
            result.append(period.getMonths()).append(" months");
        }
        if (period.getDays() == 1) {
            result.append("1 day");
        } else if (period.getDays() > 0) {
            result.append(period.getDays()).append(" days");
        }

        return result.toString();
    }
}
