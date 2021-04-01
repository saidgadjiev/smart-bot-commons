package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.Caching;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.util.concurrent.TimeUnit;

@Repository
@Caching
public class RedisUserDao implements UserDao {

    private static final String LOCALE_KEY = "locale";

    private static final String LAST_ACTIVITY_UPDATED_KEY = "last_activity_updated";

    private RedisTemplate<String, Object> redisTemplate;

    private UserDao userDao;

    @Autowired
    public RedisUserDao(RedisTemplate<String, Object> redisTemplate, @DB UserDao userDao) {
        this.redisTemplate = redisTemplate;
        this.userDao = userDao;
    }

    @Override
    public int updateActivity(int userId, String userName) {
        String key = getLastActivityUpdatedKey(userId);
        if (BooleanUtils.isTrue(redisTemplate.hasKey(key))) {
            return 1;
        }
        int updated = userDao.updateActivity(userId, userName);

        if (updated > 0) {
            setLastActivityUpdatedMarker(userId);
        }

        return updated;
    }

    @Override
    public String createOrUpdate(TgUser user) {
        String createOrUpdate = userDao.createOrUpdate(user);
        cacheLocale(user.getUserId(), user.getLanguageCode());
        setLastActivityUpdatedMarker(user.getUserId());

        return createOrUpdate;
    }

    @Override
    public void updateLocale(int userId, String language) {
        userDao.updateLocale(userId, language);

        cacheLocale(userId, language);
    }

    @Override
    public void blockUser(int userId) {
        userDao.blockUser(userId);

        String localeKey = getLocaleKey(userId);
        redisTemplate.delete(localeKey);
        String lastActivityUpdatedKey = getLastActivityUpdatedKey(userId);
        redisTemplate.delete(lastActivityUpdatedKey);
    }

    @Override
    public String getLocale(int userId) {
        String localeKey = getLocaleKey(userId);
        String locale = (String) redisTemplate.opsForValue().get(localeKey);
        if (StringUtils.isNotBlank(locale)) {
            return locale;
        }

        locale = userDao.getLocale(userId);
        cacheLocale(userId, locale);

        return locale;
    }

    @Override
    public Long countActiveUsers(int intervalInDays) {
        return userDao.countActiveUsers(intervalInDays);
    }

    private void cacheLocale(int userId, String locale) {
        String localeKey = getLocaleKey(userId);
        redisTemplate.opsForValue().set(localeKey, locale);
        redisTemplate.expire(localeKey, 2, TimeUnit.DAYS);
    }

    private void setLastActivityUpdatedMarker(int userId) {
        String key = getLastActivityUpdatedKey(userId);
        redisTemplate.opsForValue().set(key, true);
        redisTemplate.expire(key, TimeUtils.getSecondsToTheEndOfTheCurrentDay(60), TimeUnit.SECONDS);
    }

    private String getLocaleKey(int userId) {
        return LOCALE_KEY + ":" + userId;
    }

    private String getLastActivityUpdatedKey(int userId) {
        return LAST_ACTIVITY_UPDATED_KEY + ":" + userId;
    }
}