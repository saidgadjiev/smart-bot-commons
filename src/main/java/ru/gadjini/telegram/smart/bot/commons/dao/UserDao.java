package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;

public interface UserDao {

    int updateActivity(long userId, String userName);

    CreateOrUpdateResult.State createOrUpdate(TgUser user);

    void updateLocale(long userId, String language);

    void blockUser(long userId);

    String getLocale(long userId);

    Long getId(String uname);

    Long countActiveUsers(int intervalInDays);
}
