package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.Caching;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@Caching
public class RedisPaidSubscriptionDao implements PaidSubscriptionDao {

    private static final String KEY = "paid:subscription";

    private PaidSubscriptionDao paidSubscriptionDao;

    private StringRedisTemplate stringRedisTemplate;

    private RedisTemplate<String, Object> redisTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public RedisPaidSubscriptionDao(@DB PaidSubscriptionDao paidSubscriptionDao, StringRedisTemplate stringRedisTemplate,
                                    RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void create(PaidSubscription paidSubscription) {
        paidSubscriptionDao.create(paidSubscription);
        storeToRedis(paidSubscription);
    }

    public PaidSubscription getPaidSubscription(String botName, int userId) {
        PaidSubscription fromRedis = getFromRedis(botName, userId);

        if (fromRedis != null) {
            return fromRedis;
        }
        PaidSubscription fromDb = paidSubscriptionDao.getPaidSubscription(botName, userId);

        if (fromDb != null) {
            storeToRedis(fromDb);
        }

        return fromDb;
    }

    private PaidSubscription getFromRedis(String botName, int userId) {
        String key = getKey(botName, userId);

        if (redisTemplate.hasKey(key)) {
            List<Object> objects = stringRedisTemplate.opsForHash()
                    .multiGet(key, List.of(PaidSubscription.PLAN_ID, PaidSubscription.END_DATE));

            try {
                PaidSubscription subscription = new PaidSubscription();

                subscription.setPlanId(objects.get(0) == null ? null : objectMapper.readValue((String) objects.get(0), Integer.class));
                subscription.setEndDate(objectMapper.readValue((String) objects.get(1), LocalDate.class));
                subscription.setUserId(userId);

                return subscription;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    private void storeToRedis(PaidSubscription subscription) {
        String key = getKey(subscription.getBotName(), subscription.getUserId());

        Map<String, Object> values = new HashMap<>();
        if (subscription.getPlanId() != null) {
            values.put(PaidSubscription.PLAN_ID, subscription.getPlanId());
        }
        values.put(PaidSubscription.END_DATE, subscription.getEndDate());

        redisTemplate.opsForHash().putAll(key, values);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(String botName, int userId) {
        return KEY + ":" + botName + userId;
    }
}
