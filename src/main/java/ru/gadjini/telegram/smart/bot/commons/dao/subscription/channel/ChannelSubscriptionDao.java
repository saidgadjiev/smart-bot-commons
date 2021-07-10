package ru.gadjini.telegram.smart.bot.commons.dao.subscription.channel;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.concurrent.TimeUnit;

@Repository
public class ChannelSubscriptionDao {

    private static final String KEY = "sub";

    private StringRedisTemplate stringRedisTemplate;

    private MessageService messageService;

    @Autowired
    public ChannelSubscriptionDao(StringRedisTemplate stringRedisTemplate, @TgMessageLimitsControl MessageService messageService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageService = messageService;
    }

    public boolean isChatMember(String chatId, long userId, long ttl, TimeUnit timeUnit) {
        String key = key(userId);
        boolean aBoolean = BooleanUtils.toBoolean(stringRedisTemplate.hasKey(key));

        if (aBoolean) {
            return true;
        } else {
            boolean chatMember = messageService.isChatMember(chatId, userId);

            if (chatMember) {
                stringRedisTemplate.opsForValue().set(key, Boolean.TRUE.toString());
                stringRedisTemplate.expire(key, ttl, timeUnit);

                return true;
            } else {
                return false;
            }
        }
    }

    private String key(long userId) {
        return KEY + ":" + userId;
    }
}
