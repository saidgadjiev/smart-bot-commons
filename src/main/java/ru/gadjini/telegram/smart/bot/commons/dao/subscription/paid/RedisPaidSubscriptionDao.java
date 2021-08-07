package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@Redis
public class RedisPaidSubscriptionDao implements PaidSubscriptionDao {

    private static final String KEY = "paid:subscription";

    private PaidSubscriptionDao paidSubscriptionDao;

    private StringRedisTemplate stringRedisTemplate;

    private RedisTemplate<String, Object> redisTemplate;

    private Jackson json;

    @Autowired
    public RedisPaidSubscriptionDao(@DB PaidSubscriptionDao paidSubscriptionDao, StringRedisTemplate stringRedisTemplate,
                                    RedisTemplate<String, Object> redisTemplate, Jackson json) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.json = json;
    }

    @Override
    public PaidSubscription activateSubscriptionDay(long userId) {
        PaidSubscription paidSubscription = paidSubscriptionDao.activateSubscriptionDay(userId);
        updateEndDateSubscriptionInterval(userId, paidSubscription.getEndDate(), paidSubscription.getSubscriptionInterval());

        return paidSubscription;
    }

    @Override
    public void create(PaidSubscription paidSubscription) {
        paidSubscriptionDao.create(paidSubscription);
        storeToRedis(paidSubscription);
    }

    @Override
    public PaidSubscription getByBotNameAndUserId(long userId) {
        PaidSubscription fromRedis = getFromRedis(userId);

        if (fromRedis != null) {
            return fromRedis;
        }
        PaidSubscription fromDb = paidSubscriptionDao.getByBotNameAndUserId(userId);

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
    public int remove(long userId) {
        int remove = paidSubscriptionDao.remove(userId);
        String key = getKey(userId);
        redisTemplate.delete(key);

        return remove;
    }

    @Override
    public void refresh(long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    @Override
    public void refreshAll() {
        Set<String> keys = redisTemplate.keys(KEY + "*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    private PaidSubscription getFromRedis(long userId) {
        String key = getKey(userId);

        if (redisTemplate.hasKey(key)) {
            List<Object> objects = stringRedisTemplate.opsForHash()
                    .multiGet(key, List.of(PaidSubscription.PLAN_ID, PaidSubscription.END_DATE, PaidSubscription.PURCHASE_DATE,
                            PaidSubscription.SUBSCRIPTION_INTERVAL));

            PaidSubscription subscription = new PaidSubscription();

            subscription.setPlanId(objects.get(0) == null ? null : json.readValue((String) objects.get(0), Integer.class));
            subscription.setEndDate(StringUtils.isBlank((String) objects.get(1)) ? null : json.readValue((String) objects.get(1), LocalDate.class));
            subscription.setPurchaseDate(json.readValue((String) objects.get(2), ZonedDateTime.class));
            subscription.setSubscriptionInterval(StringUtils.isBlank((String) objects.get(3)) ? null : json.readValue((String) objects.get(3), Period.class));

            subscription.setUserId(userId);

            return subscription;
        }

        return null;
    }

    private void updateEndDateSubscriptionInterval(long userId, LocalDate endDate, Period subscriptionInterval) {
        String key = getKey(userId);

        redisTemplate.opsForHash().put(key, PaidSubscription.END_DATE, endDate);
        redisTemplate.opsForHash().put(key, PaidSubscription.SUBSCRIPTION_INTERVAL, subscriptionInterval);
    }

    private void storeToRedis(PaidSubscription subscription) {
        String key = getKey(subscription.getUserId());

        Map<String, Object> values = new HashMap<>();
        if (subscription.getPlanId() != null) {
            values.put(PaidSubscription.PLAN_ID, subscription.getPlanId());
        }
        values.put(PaidSubscription.END_DATE, subscription.getEndDate());
        values.put(PaidSubscription.PURCHASE_DATE, subscription.getPurchaseDate());
        values.put(PaidSubscription.SUBSCRIPTION_INTERVAL, subscription.getSubscriptionInterval());

        redisTemplate.opsForHash().putAll(key, values);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(long userId) {
        return KEY + ":" + userId;
    }
}
