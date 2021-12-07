package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;

@Repository
@Redis
public class RedisBlackListDao implements BlackListDao {

    private static final String BLACK_LIST = "black_list";

    private BlackListDao dbBlackListDao;

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisBlackListDao(@DB BlackListDao dbBlackListDao, RedisTemplate<String, Object> redisTemplate) {
        this.dbBlackListDao = dbBlackListDao;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Boolean isInBlackList(long userId) {
        String key = String.valueOf(userId);

        if (redisTemplate.opsForHash().hasKey(BLACK_LIST, key)) {
            return (Boolean) redisTemplate.opsForHash().get(BLACK_LIST, key);
        }

        Boolean inBlackList = dbBlackListDao.isInBlackList(userId);
        redisTemplate.opsForHash().put(BLACK_LIST, key, inBlackList);

        return inBlackList;
    }
}
