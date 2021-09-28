package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class SubscriptionProperties {

    @Value("${check.channel.subscription:false}")
    private boolean checkChannelSubscription;

    @Value("${check.paid.subscription:false}")
    private boolean checkPaidSubscription;

    @Value("${trial.period:7}")
    private int trialPeriod;

    @Value("${payment.bot.name}")
    private String paymentBotName;

    @Value("${payment.bot.server}")
    private String paymentBotServer;

    @Value("${trial.max.file.size:104857600}")
    private long trialMaxFileSize;

    @Value("${trial.max.actions.count:5}")
    private int trialMaxActionsCount;

    public boolean isCheckChannelSubscription() {
        return checkChannelSubscription;
    }

    public void setCheckChannelSubscription(boolean checkChannelSubscription) {
        this.checkChannelSubscription = checkChannelSubscription;
    }

    public boolean isCheckPaidSubscription() {
        return checkPaidSubscription;
    }

    public void setCheckPaidSubscription(boolean checkPaidSubscription) {
        this.checkPaidSubscription = checkPaidSubscription;
    }

    public int getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(int trialPeriod) {
        this.trialPeriod = trialPeriod;
    }

    public String getPaymentBotName() {
        return paymentBotName;
    }

    public void setPaymentBotName(String paymentBotName) {
        this.paymentBotName = paymentBotName;
    }

    public String getPaymentBotServer() {
        return paymentBotServer;
    }

    public long getTrialMaxFileSize() {
        return trialMaxFileSize;
    }

    public void setTrialMaxFileSize(long trialMaxFileSize) {
        this.trialMaxFileSize = trialMaxFileSize;
    }

    public int getTrialMaxActionsCount() {
        return trialMaxActionsCount;
    }

    public void setTrialMaxActionsCount(int trialMaxActionsCount) {
        this.trialMaxActionsCount = trialMaxActionsCount;
    }
}
