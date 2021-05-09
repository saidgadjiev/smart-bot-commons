package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;

public interface UserDao {

    int updateActivity(int userId, String userName);

    CreateOrUpdateResult.State createOrUpdate(TgUser user);

    void updateLocale(int userId, String language);

    void blockUser(int userId);

    String getLocale(int userId);

    Long countActiveUsers(int intervalInDays);
}
