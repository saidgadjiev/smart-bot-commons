package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

import java.util.List;

@Component
public class StaticTextAppender {

    private TutorialService tutorialService;

    @Autowired
    public StaticTextAppender(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    public String process(String command, String sourceText) {
        List<Tutorial> tutorials = tutorialService.getTutorials(command);

        if (!tutorials.isEmpty()) {
            StringBuilder tutorialsBuilder = new StringBuilder();
            for (int i = 0; i < tutorials.size(); i++) {
                Tutorial tutorial = tutorials.get(i);
                if (tutorialsBuilder.length() > 0) {
                    tutorialsBuilder.append("\n");
                }
                tutorialsBuilder.append(i).append(") /tutorial_").append(tutorial.getId()).append(" - <i>")
                        .append(tutorial.getDescription()).append("</i>");
            }

            return sourceText + "\n\n<b>Tutorials:</b>\n" + tutorialsBuilder.toString();
        }

        return sourceText;
    }
}
