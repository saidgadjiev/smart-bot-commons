package ru.gadjini.telegram.smart.bot.commons.service;

import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;

public class DefaultMediaExtractor implements UrlMediaExtractor {

    @Override
    public MessageMedia extractMedia(long userId, String url) {
        return null;
    }
}
