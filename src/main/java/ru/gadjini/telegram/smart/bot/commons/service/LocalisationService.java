package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class LocalisationService {

    public static final String RU_LOCALE = "ru";

    public static final String EN_LOCALE = "en";

    private MessageSource messageSource;

    @Autowired
    public LocalisationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String messageCode, Locale locale) {
        return getMessage(messageCode, null, locale);
    }

    public String getMessage(String messageCode, Locale locale, String defaultMsg) {
        try {
            return getMessage(messageCode, null, locale);
        } catch (Exception e) {
            return defaultMsg;
        }
    }

    public String getMessage(String messageCode, Object[] args, Locale locale) {
        return messageSource.getMessage(messageCode, args, locale);
    }

    public List<Locale> getSupportedLocales() {
        return List.of(new Locale(RU_LOCALE), new Locale(EN_LOCALE));
    }
}
