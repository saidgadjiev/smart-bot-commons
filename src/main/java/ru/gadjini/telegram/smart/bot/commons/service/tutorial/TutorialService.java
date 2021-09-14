package ru.gadjini.telegram.smart.bot.commons.service.tutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.TutorialDao;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;

import java.util.List;

@Service
public class TutorialService {

    private TutorialDao tutorialDao;

    private BotProperties botProperties;

    @Autowired
    public TutorialService(TutorialDao tutorialDao, BotProperties botProperties) {
        this.tutorialDao = tutorialDao;
        this.botProperties = botProperties;
    }

    public void create(Tutorial tutorial) {
        tutorial.setBotName(botProperties.getName());
        tutorialDao.create(tutorial);
    }

    public List<Tutorial> getTutorials(String command) {
        return tutorialDao.getTutorials(command, botProperties.getName());
    }

    public List<Tutorial> getTutorials() {
        return tutorialDao.getTutorials(botProperties.getName());
    }

    public String getFileId(int tutorialId) {
        return tutorialDao.getFileId(tutorialId);
    }
}
