package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

import java.util.List;

@Component
public class TutorialMessageBuilder {

    private TutorialService tutorialService;

    @Autowired
    public TutorialMessageBuilder(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    public String buildTutorialsMessage(String command) {
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

            return "<b>Tutorials:</b>\n" + tutorialsBuilder.toString();
        }

        return null;
    }
}
