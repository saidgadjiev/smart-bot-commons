package ru.gadjini.telegram.smart.bot.commons.event;

public class RefreshPaidSubscriptionEvent {

    private long userId;

    private String sourceBotName;

    public RefreshPaidSubscriptionEvent(long userId, String sourceBotName) {
        this.userId = userId;
        this.sourceBotName = sourceBotName;
    }

    public long getUserId() {
        return userId;
    }

    public String getSourceBotName() {
        return sourceBotName;
    }
}
