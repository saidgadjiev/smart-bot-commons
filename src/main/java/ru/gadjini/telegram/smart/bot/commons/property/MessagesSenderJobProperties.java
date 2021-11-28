package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("messages.sender.job")
public class MessagesSenderJobProperties {

    @Value("${disable:false}")
    private boolean disable;

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

}
