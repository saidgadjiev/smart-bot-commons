package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

import java.util.List;
import java.util.Locale;

@Component
public class TutorialMessageBuilder {

    private TutorialService tutorialService;

    private LocalisationService localisationService;

    @Autowired
    public TutorialMessageBuilder(TutorialService tutorialService, LocalisationService localisationService) {
        this.tutorialService = tutorialService;
        this.localisationService = localisationService;
    }

    public String buildTutorialsMessage(String command, Locale locale) {
        List<Tutorial> tutorials = tutorialService.getTutorials(command);

        if (!tutorials.isEmpty()) {
            StringBuilder tutorialsBuilder = new StringBuilder();
            for (int i = 0; i < tutorials.size(); i++) {
                Tutorial tutorial = tutorials.get(i);
                if (tutorialsBuilder.length() > 0) {
                    tutorialsBuilder.append("\n");
                }
                tutorialsBuilder.append(i + 1).append(") /tutorial_").append(tutorial.getId()).append(" - <i>")
                        .append(tutorial.getDescription()).append("</i>");
            }

            return localisationService.getMessage(MessagesProperties.MESSAGE_TUTORIALS_LABEL, locale)
                    + "\n" + tutorialsBuilder.toString();
        }

        return null;
    }
}
