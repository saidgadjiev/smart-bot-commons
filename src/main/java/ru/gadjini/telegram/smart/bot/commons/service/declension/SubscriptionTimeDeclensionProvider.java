package ru.gadjini.telegram.smart.bot.commons.service.declension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubscriptionTimeDeclensionProvider {

    private Map<String, SubscriptionTimeDeclensionService> declensionServiceMap = new HashMap<>();

    @Autowired
    public SubscriptionTimeDeclensionProvider(Collection<SubscriptionTimeDeclensionService> declensionServices) {
        declensionServices.forEach(timeDeclensionService -> declensionServiceMap.put(timeDeclensionService.getLocale(), timeDeclensionService));
    }

    public SubscriptionTimeDeclensionService getService(String language) {
        return declensionServiceMap.get(language);
    }
}
