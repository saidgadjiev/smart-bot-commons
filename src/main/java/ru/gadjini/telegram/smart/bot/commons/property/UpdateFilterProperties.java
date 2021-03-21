package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("update")
public class UpdateFilterProperties {

    private boolean acceptPayments;

    public boolean isAcceptPayments() {
        return acceptPayments;
    }

    public void setAcceptPayments(boolean acceptPayments) {
        this.acceptPayments = acceptPayments;
    }
}
