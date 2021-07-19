package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;

@Component
@ConditionalOnMissingBean(UrlMediaExtractor.class)
public class DefaultMediaExtractor implements UrlMediaExtractor {

    @Override
    public MessageMedia extractMedia(long userId, String url) {
        return null;
    }
}
