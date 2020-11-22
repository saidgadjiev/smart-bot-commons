package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("file.manager")
public class FileManagerProperties {

    private long sleepTimeBeforeDownloadAttempt = 90000;

    private long sleepTimeBeforeUploadAttempt = 60000;

    private int maxDownloadAttempts = 2;

    private int maxUploadAttempts = 2;

    public long getSleepTimeBeforeDownloadAttempt() {
        return sleepTimeBeforeDownloadAttempt;
    }

    public void setSleepTimeBeforeDownloadAttempt(long sleepTimeBeforeDownloadAttempt) {
        this.sleepTimeBeforeDownloadAttempt = sleepTimeBeforeDownloadAttempt;
    }

    public long getSleepTimeBeforeUploadAttempt() {
        return sleepTimeBeforeUploadAttempt;
    }

    public void setSleepTimeBeforeUploadAttempt(long sleepTimeBeforeUploadAttempt) {
        this.sleepTimeBeforeUploadAttempt = sleepTimeBeforeUploadAttempt;
    }

    public int getMaxDownloadAttempts() {
        return maxDownloadAttempts;
    }

    public void setMaxDownloadAttempts(int maxDownloadAttempts) {
        this.maxDownloadAttempts = maxDownloadAttempts;
    }

    public int getMaxUploadAttempts() {
        return maxUploadAttempts;
    }

    public void setMaxUploadAttempts(int maxUploadAttempts) {
        this.maxUploadAttempts = maxUploadAttempts;
    }
}