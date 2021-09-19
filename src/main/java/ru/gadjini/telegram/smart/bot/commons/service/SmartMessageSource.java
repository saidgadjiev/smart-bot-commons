package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

@Component
public class SmartMessageSource {

    private static final String COMMON_MESSAGES = "common-messages.properties";

    private MessageSource messageSource;

    @Autowired
    public SmartMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
        if (messageSource instanceof AbstractMessageSource) {
            Properties properties = new Properties();
            try (InputStream stream = LocalisationService.class.getClassLoader().getResourceAsStream(COMMON_MESSAGES)) {
                properties.load(stream);

                ((AbstractMessageSource) messageSource).setCommonMessages(properties);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
}
