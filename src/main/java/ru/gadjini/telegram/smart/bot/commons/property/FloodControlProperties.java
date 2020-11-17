package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("flood.control")
public class FloodControlProperties {

    private int sleepAfterXDownloads = 1;

    //In seconds
    private int maxSleepTime = 20;

    //In seconds
    private int minSleepTime = 2;

    private int fileDownloadingConcurrencyLevel = 1;

    private boolean enableLogging = false;

    public int getMaxSleepTime() {
        return maxSleepTime;
    }

    public void setMaxSleepTime(int maxSleepTime) {
        this.maxSleepTime = maxSleepTime;
    }

    public int getSleepAfterXDownloads() {
        return sleepAfterXDownloads;
    }

    public void setSleepAfterXDownloads(int sleepAfterXDownloads) {
        this.sleepAfterXDownloads = sleepAfterXDownloads;
    }

    public int getFileDownloadingConcurrencyLevel() {
        return fileDownloadingConcurrencyLevel;
    }

    public void setFileDownloadingConcurrencyLevel(int fileDownloadingConcurrencyLevel) {
        this.fileDownloadingConcurrencyLevel = fileDownloadingConcurrencyLevel;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public int getMinSleepTime() {
        return minSleepTime;
    }

    public void setMinSleepTime(int minSleepTime) {
        this.minSleepTime = minSleepTime;
    }
}
