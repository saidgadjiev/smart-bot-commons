package ru.gadjini.telegram.smart.bot.commons.dao.tutorial;

import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;

import java.util.List;

public interface TutorialDao {

    void delete(int id);

    void create(Tutorial tutorial);

    List<Tutorial> getTutorials(String command, String botName);

    String getFileId(int id);

    List<Tutorial> getTutorials(String botName);
}
