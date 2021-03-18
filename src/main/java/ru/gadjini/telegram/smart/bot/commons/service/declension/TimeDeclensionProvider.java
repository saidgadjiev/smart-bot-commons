package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimeDeclensionProvider {

    private Map<String, TimeDeclensionService> declensionServiceMap = new HashMap<>();

    @Autowired
    public TimeDeclensionProvider(Collection<TimeDeclensionService> declensionServices) {
        declensionServices.forEach(timeDeclensionService -> declensionServiceMap.put(timeDeclensionService.getLocale(), timeDeclensionService));
    }

    public TimeDeclensionService getService(String language) {
        return declensionServiceMap.get(language);
    }
}
