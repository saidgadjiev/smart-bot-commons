package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class EnTimeDeclensionService implements TimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.EN_LOCALE;
    }

    @Override
    public String day(int days) {
        if (days == 1) {
            return "day";
        }

        return days + " days";
    }

    @Override
    public String months(int months) {
        if (months == 1) {
            return "month";
        }

        return months + " months";
    }
}
