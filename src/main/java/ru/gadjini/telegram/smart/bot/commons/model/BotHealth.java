package ru.gadjini.telegram.smart.bot.commons.model;

public class BotHealth {

    private String message;

    private String commitId;

    private String profile;

    private int ffmpegScannersCount;

    public BotHealth(String message, String commitId, String profile, int ffmpegScannersCount) {
        this.message = message;
        this.commitId = commitId;
        this.profile = profile;
        this.ffmpegScannersCount = ffmpegScannersCount;
    }

    public String getMessage() {
        return message;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getProfile() {
        return profile;
    }

    public int getFfmpegScannersCount() {
        return ffmpegScannersCount;
    }
}
