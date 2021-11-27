package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.TutorialMessageBuilder;

import java.util.List;
import java.util.Locale;

@Component
public class LocalisationService {

    public static final String RU_LOCALE = "ru";

    public static final String EN_LOCALE = "en";

    public static final String UZ_LOCALE = "uz";

    private SmartMessageSource smartMessageSource;

    private TutorialMessageBuilder tutorialMessageBuilder;

    @Autowired
    public LocalisationService(SmartMessageSource smartMessageSource, TutorialMessageBuilder tutorialMessageBuilder) {
        this.smartMessageSource = smartMessageSource;
        this.tutorialMessageBuilder = tutorialMessageBuilder;
    }

    public String getCommandWelcomeMessage(String command, String messageCode, Locale locale) {
        return getCommandWelcomeMessage(command, messageCode, null, locale);
    }

    public String getCommandWelcomeMessage(String command, String messageCode, Object[] args, Locale locale) {
        String tutorialsMessage = tutorialMessageBuilder.buildTutorialsMessage(command);

        if (StringUtils.isNotBlank(tutorialsMessage)) {
            return smartMessageSource.getMessage(
                    messageCode, args, locale
            ) + "\n\n" + getMessage(MessagesProperties.MESSAGE_TUTORIALS_LABEL, new Object[] {tutorialsMessage}, locale);
        } else {
            return smartMessageSource.getMessage(
                    messageCode, args, locale
            );
        }
    }

    public String getMessage(String messageCode, Locale locale) {
        return smartMessageSource.getMessage(messageCode, locale);
    }

    public String getMessage(String messageCode, Locale locale, String defaultMsg) {
        return smartMessageSource.getMessage(messageCode, locale, defaultMsg);
    }

    public String getMessage(String messageCode, Object[] args, Locale locale) {
        return smartMessageSource.getMessage(messageCode, args, locale);
    }

    public List<Locale> getSupportedLocales() {
        return List.of(new Locale(RU_LOCALE), new Locale(EN_LOCALE), new Locale(UZ_LOCALE));
    }
}
