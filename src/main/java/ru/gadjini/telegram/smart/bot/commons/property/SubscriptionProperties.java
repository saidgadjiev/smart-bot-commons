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

    @Value("#{'${paid.bot.name:${bot.name}}'}")
    private String paidBotName;

    @Value("${payment.currency:USD}")
    private String paymentCurrency;

    @Value("${payment.description:true}")
    private boolean paymentDescription;

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

    public String getPaidBotName() {
        return paidBotName;
    }

    public void setPaidBotName(String paidBotName) {
        this.paidBotName = paidBotName;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public boolean isPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(boolean paymentDescription) {
        this.paymentDescription = paymentDescription;
    }
}
