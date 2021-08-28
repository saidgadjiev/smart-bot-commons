package ru.gadjini.telegram.smart.bot.commons.model;

public class RefreshPaidSubscriptionRequest {

    private String sourceBotName;

    public RefreshPaidSubscriptionRequest() {

    }

    public RefreshPaidSubscriptionRequest(String sourceBotName) {
        this.sourceBotName = sourceBotName;
    }

    public String getSourceBotName() {
        return sourceBotName;
    }

    public void setSourceBotName(String sourceBotName) {
        this.sourceBotName = sourceBotName;
    }
}
