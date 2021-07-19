package ru.gadjini.telegram.smart.bot.commons.service;

import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;

public interface UrlMediaExtractor {

    MessageMedia extractMedia(long userId, String url);
}
