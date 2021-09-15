package ru.gadjini.telegram.smart.bot.commons.dao.tutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Redis
public class RedisTutorialDao implements TutorialDao {

    private static final String KEY = "tutorial";

    private TutorialDao tutorialDao;

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisTutorialDao(@DB TutorialDao tutorialDao, RedisTemplate<String, Object> redisTemplate) {
        this.tutorialDao = tutorialDao;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void delete(int id) {
        tutorialDao.delete(id);
        redisTemplate.delete(KEY);
    }

    @Override
    public void create(Tutorial tutorial) {
        tutorialDao.create(tutorial);
    }

    @Override
    public List<Tutorial> getTutorials(String command, String botName) {
        if (redisTemplate.hasKey(KEY)) {
            List<Object> list = redisTemplate.opsForList().range(KEY, 0, -1);

            return ((List<Object>) list.get(0)).stream().map(s -> map((Map<String, Object>) s)).collect(Collectors.toList());
        } else {
            List<Tutorial> tutorials = tutorialDao.getTutorials(command, botName);
            redisTemplate.opsForList().rightPushAll(KEY, tutorials);

            return tutorials;
        }
    }

    @Override
    public String getFileId(int id) {
        return tutorialDao.getFileId(id);
    }

    @Override
    public List<Tutorial> getTutorials(String botName) {
        return tutorialDao.getTutorials(botName);
    }

    private Tutorial map(Map<String, Object> values) {
        Tutorial tutorial = new Tutorial();
        tutorial.setId((Integer) values.get(Tutorial.ID));
        tutorial.setDescription((String) values.get(Tutorial.DESCRIPTION));
        tutorial.setCommand((String) values.get(Tutorial.DESCRIPTION));

        return tutorial;
    }
}
