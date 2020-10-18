package ru.gadjini.telegram.smart.bot.commons.property;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("file.limit")
public class FileLimitProperties {

    public static final int FLOOD_WAIT_SLEEP_TIME = 30000;

    public static final int MAX_ATTEMPTS = 20;

    @Value("${light.file.max.weight:104857600}")
    private long lightFileMaxWeight;

    public long getLightFileMaxWeight() {
        return lightFileMaxWeight;
    }

    public void setLightFileMaxWeight(long lightFileMaxWeight) {
        this.lightFileMaxWeight = lightFileMaxWeight;
    }
}
