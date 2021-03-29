package ru.gadjini.telegram.smart.bot.commons.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class PaidSubscription {

    public static final String TABLE = "paid_subscription";

    public static final String USER_ID = "user_id";

    public static final String END_DATE = "end_date";

    public static final String BOT_NAME = "bot_name";

    public static final String PURCHASE_DATE = "purchase_date";

    public static final String PLAN_ID = "plan_id";

    private int userId;

    private LocalDate endDate;

    private LocalDateTime purchaseDate;

    private String botName;

    private Integer planId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isTrial() {
        return planId == null;
    }

    public boolean isActive() {
        return LocalDate.now(ZoneOffset.UTC).isBefore(endDate);
    }
}
