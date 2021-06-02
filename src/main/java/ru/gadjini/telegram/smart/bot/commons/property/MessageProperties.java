package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("watermark")
public class MessageProperties {

    private Map<String, String> referral;

    public Map<String, String> getReferral() {
        return referral;
    }

    public void setReferral(Map<String, String> referral) {
        this.referral = referral;
    }

    public String getWatermarkReferral(String chatId) {
        return referral == null ? null : referral.get(chatId);
    }
}
