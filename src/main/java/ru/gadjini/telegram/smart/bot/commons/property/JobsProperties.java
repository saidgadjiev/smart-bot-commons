package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jobs")
public class JobsProperties {

    private boolean disable;

    private boolean enableLogging;

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
