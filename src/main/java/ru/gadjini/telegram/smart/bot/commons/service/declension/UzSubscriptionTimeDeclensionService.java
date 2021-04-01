package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class UzSubscriptionTimeDeclensionService implements SubscriptionTimeDeclensionService {

    @Override
    public String getLocale() {
        return LocalisationService.UZ_LOCALE;
    }

    @Override
    public String months(int months) {
        return months + " oy";
    }
}
