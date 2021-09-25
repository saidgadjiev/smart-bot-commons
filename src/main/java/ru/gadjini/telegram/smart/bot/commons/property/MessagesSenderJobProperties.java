package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("messages.sender.job")
public class MessagesSenderJobProperties {

    @Value("${disable:false}")
    private boolean disable;

    @Value("${disable.async:false}")
    private boolean disableAsync;

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isDisableAsync() {
        return disableAsync;
    }

    public void setDisableAsync(boolean disableAsync) {
        this.disableAsync = disableAsync;
    }
}
