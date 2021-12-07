package ru.gadjini.telegram.smart.bot.commons.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.BlackListDao;

@Service
public class BlackListService {

    private BlackListDao blackListDao;

    @Autowired
    public BlackListService(@Redis BlackListDao blackListDao) {
        this.blackListDao = blackListDao;
    }

    public boolean isInBlackList(long userId) {
        return blackListDao.isInBlackList(userId);
    }
}
