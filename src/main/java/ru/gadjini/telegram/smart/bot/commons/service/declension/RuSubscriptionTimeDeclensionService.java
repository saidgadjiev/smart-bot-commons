package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class RuSubscriptionTimeDeclensionService implements SubscriptionTimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.RU_LOCALE;
    }

    @Override
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
    public String months(int months) {
        if (months == 1) {
            return "1 месяц";
        }
        if (months >= 2 && months <= 4) {
            return months + " месяца";
        }

        return months + " месяцев";
    }
}