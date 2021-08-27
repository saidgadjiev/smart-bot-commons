package ru.gadjini.telegram.smart.bot.commons.domain;

import org.joda.time.Period;

import java.time.ZonedDateTime;

public class PaidSubscription {

    public static final String TABLE = "paid_subscription";

    public static final String USER_ID = "user_id";

    public static final String END_AT = "end_at";

    public static final String PURCHASED_AT = "purchased_at";

    public static final String PLAN_ID = "plan_id";

    public static final String SUBSCRIPTION_INTERVAL = "subscription_interval";

    private long userId;

    private ZonedDateTime endAt;

    private Period subscriptionInterval;

    private ZonedDateTime purchasedAt;

    private Integer planId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public ZonedDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(ZonedDateTime endAt) {
        this.endAt = endAt;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public ZonedDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(ZonedDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Period getSubscriptionInterval() {
        return subscriptionInterval;
    }

    public void setSubscriptionInterval(Period subscriptionInterval) {
        this.subscriptionInterval = subscriptionInterval;
    }

    public boolean isTrial() {
        return planId == null;
    }
}
