package ru.gadjini.telegram.smart.bot.commons.service.tutorial;

import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.TutorialDao;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;

import java.util.List;

@Service
public class TutorialService {

    private TutorialDao tutorialDao;

    public void create(Tutorial tutorial) {

    }

    public List<Tutorial> getTutorials(String command) {
        return tutorialDao.getTutorials(command);
    }

    public List<Tutorial> getTutorials() {
        return tutorialDao.getTutorials();
    }

    public String getFileId(int tutorialId) {
        return tutorialDao.getFileId(tutorialId);
    }
}
