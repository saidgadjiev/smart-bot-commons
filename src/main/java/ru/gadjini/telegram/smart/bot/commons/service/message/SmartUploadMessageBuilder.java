package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

@Service
public class SmartUploadMessageBuilder {

    private LocalisationService localisationService;

    @Autowired
    public SmartUploadMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public String buildSmartUploadMessage(UploadQueueItem queueItem) {

    }
}
