package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("flood.control")
public class FloodControlProperties {

    private int sleepAfterXDownloads = 1;

    //In seconds
    private int sleepTime = 5;

    private int fileDownloadingConcurrencyLevel = 1;

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
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
}
