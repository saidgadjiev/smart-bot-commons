package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("progress")
public class ProgressProperties {

    @Value("${time.to.update:5}")
    private long timeToUpdate;

    @Value("${disabled:false}")
    private boolean disabled;

    public long getTimeToUpdate() {
        return timeToUpdate;
    }

    public void setTimeToUpdate(long timeToUpdate) {
        this.timeToUpdate = timeToUpdate;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
