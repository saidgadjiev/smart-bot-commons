package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@Redis
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

    @Override
    public void create(PaidSubscription paidSubscription) {
        paidSubscriptionDao.create(paidSubscription);
        storeToRedis(paidSubscription);
    }

    @Override
    public PaidSubscription getByBotNameAndUserId(String botName, int userId) {
        PaidSubscription fromRedis = getFromRedis(botName, userId);

        if (fromRedis != null) {
            return fromRedis;
        }
        PaidSubscription fromDb = paidSubscriptionDao.getByBotNameAndUserId(botName, userId);

        if (fromDb != null) {
            storeToRedis(fromDb);
        }

        return fromDb;
    }

    @Override
    public void createOrRenew(PaidSubscription paidSubscription, Period period) {
        paidSubscriptionDao.createOrRenew(paidSubscription, period);
        storeToRedis(paidSubscription);
    }

    @Override
    public int remove(String botName, int userId) {
        int remove = paidSubscriptionDao.remove(botName, userId);
        String key = getKey(botName, userId);
        redisTemplate.delete(key);

        return remove;
    }

    @Override
    public void refresh(String botName, int userId) {
        String key = getKey(botName, userId);
        redisTemplate.delete(key);
    }

    private PaidSubscription getFromRedis(String botName, int userId) {
        String key = getKey(botName, userId);

        if (redisTemplate.hasKey(key)) {
            List<Object> objects = stringRedisTemplate.opsForHash()
                    .multiGet(key, List.of(PaidSubscription.PLAN_ID, PaidSubscription.END_DATE, PaidSubscription.PURCHASE_DATE));

            try {
                PaidSubscription subscription = new PaidSubscription();

                subscription.setPlanId(objects.get(0) == null ? null : objectMapper.readValue((String) objects.get(0), Integer.class));
                subscription.setEndDate(objectMapper.readValue((String) objects.get(1), LocalDate.class));
                subscription.setPurchaseDate(objectMapper.readValue((String) objects.get(2), ZonedDateTime.class));
                subscription.setBotName(botName);
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
        values.put(PaidSubscription.PURCHASE_DATE, subscription.getPurchaseDate());

        redisTemplate.opsForHash().putAll(key, values);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(String botName, int userId) {
        return KEY + ":" + botName + userId;
    }
}
