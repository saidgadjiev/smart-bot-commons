package ru.gadjini.telegram.smart.bot.commons.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.UserBotDao;

@Component
public class UserBotService {

    private UserBotDao userBotDao;

    @Autowired
    public UserBotService(UserBotDao userBotDao) {
        this.userBotDao = userBotDao;
    }

    public boolean create(int userId, String botName) {
        return userBotDao.createIfNotExist(userId, botName);
    }
}
