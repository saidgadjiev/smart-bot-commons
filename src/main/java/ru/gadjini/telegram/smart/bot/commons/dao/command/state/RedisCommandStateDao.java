package ru.gadjini.telegram.smart.bot.commons.dao.command.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;

import java.util.concurrent.TimeUnit;

@Repository
@Qualifier("redis")
public class RedisCommandStateDao implements CommandStateDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCommandStateDao.class);

    private static final String KEY = "cmd";

    private RedisTemplate<String, Object> redisTemplate;

    private Jackson json;

    @Autowired
    public RedisCommandStateDao(RedisTemplate<String, Object> redisTemplate, Jackson json) {
        this.redisTemplate = redisTemplate;
        this.json = json;
    }

    @Override
    public void setState(long chatId, String command, Object state, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key(chatId, command), state, ttl, timeUnit);
    }

    @Override
    public void expire(long chatId, String command, long ttl, TimeUnit timeUnit) {
        redisTemplate.expire(key(chatId, command), ttl, timeUnit);
    }

    @Override
    public <T> T getState(long chatId, String command, Class<T> tClass) {
        try {
            Object o = redisTemplate.opsForValue().get(key(chatId, command));

            if (o == null) {
                return null;
            }
            if (o.getClass() == tClass) {
                return tClass.cast(o);
            }

            return json.convertValue(o, tClass);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean hasState(long chatId, String command) {
        Boolean aBoolean = redisTemplate.hasKey(key(chatId, command));

        return aBoolean != null && aBoolean;
    }

    @Override
    public void deleteState(long chatId, String command) {
        redisTemplate.delete(key(chatId, command));
    }

    private String key(long chatId, String command) {
        return KEY + ":" + command + ":" + chatId;
    }
}
