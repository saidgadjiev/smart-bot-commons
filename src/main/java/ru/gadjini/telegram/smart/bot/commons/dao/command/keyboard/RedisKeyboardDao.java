package ru.gadjini.telegram.smart.bot.commons.dao.command.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;

@Repository
@Redis
public class RedisKeyboardDao implements ReplyKeyboardDao {

    private static final String KEY = "keyboard";

    private RedisTemplate<String, Object> redisTemplate;

    private Jackson json;

    @Autowired
    public RedisKeyboardDao(RedisTemplate<String, Object> redisTemplate, Jackson json) {
        this.redisTemplate = redisTemplate;
        this.json = json;
    }

    @Override
    public void store(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        redisTemplate.opsForValue().set(getKey(chatId), replyKeyboardMarkup);
    }

    @Override
    public ReplyKeyboardMarkup get(long chatId) {
        Object o = redisTemplate.opsForValue().get(getKey(chatId));

        if (o == null) {
            return null;
        }

        return json.convertValue(o, ReplyKeyboardMarkup.class);
    }

    private String getKey(long chatId) {
        return KEY + ":" + chatId;
    }
}
