package ru.gadjini.telegram.smart.bot.commons.domain;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class PaidSubscription {

    public static final String TABLE = "paid_subscription";

    public static final String USER_ID = "user_id";

    public static final String END_DATE = "end_date";

    public static final String PURCHASE_DATE = "purchase_date";

    public static final String PLAN_ID = "plan_id";

    public static final String SUBSCRIPTION_INTERVAL = "subscription_interval";

    private long userId;

    private LocalDate endDate;

    private Period subscriptionInterval;

    private ZonedDateTime purchaseDate;

    private Integer planId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ZonedDateTime getZonedEndDate() {
        return TimeUtils.toZonedDateTime(endDate);
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public ZonedDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(ZonedDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public boolean isActive() {
        if (endDate == null) {
            return false;
        }
        LocalDate now = LocalDate.now(TimeUtils.UTC);

        return now.isBefore(endDate) || now.isEqual(endDate);
    }

    public boolean isSubscriptionIntervalActive() {
        if (subscriptionInterval == null) {
            return false;
        }
        return JodaTimeUtils.toDays(subscriptionInterval) > 0;
    }
}
