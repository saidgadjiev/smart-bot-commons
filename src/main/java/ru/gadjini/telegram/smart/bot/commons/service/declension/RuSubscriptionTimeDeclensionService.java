package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.joda.time.Period;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class RuSubscriptionTimeDeclensionService implements SubscriptionTimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.RU_LOCALE;
    }

    public String day(int days) {
        if (days == 1) {
            return "1 день";
        }
        if (days >= 2 && days <= 4) {
            return days + " дня";
        }

        return days + " дней";
    }

    @Override
    public String localize(Period period) {
        StringBuilder result = new StringBuilder();

        if (period.getYears() == 1) {
            result.append("1 год");
        } else if (period.getYears() >= 2 && period.getYears() <= 4) {
            result.append(period.getYears()).append(" года");
        } else if (period.getYears() != 0) {
            result.append(period.getYears()).append(" лет");
        }
        if (period.getMonths() == 1) {
            result.append("1 месяц");
        } else if (period.getMonths() >= 2 && period.getMonths() <= 4) {
            result.append(period.getMonths()).append(" месяца");
        } else if (period.getMonths() != 0) {
            result.append(period.getMonths()).append(" месяцев");
        }

        return result.toString();
    }
}
