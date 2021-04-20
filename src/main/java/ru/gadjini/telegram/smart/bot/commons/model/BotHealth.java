package ru.gadjini.telegram.smart.bot.commons.model;

public class BotHealth {

    private String message;

    private String commitId;

    private String profile;

    public BotHealth(String message, String commitId, String profile) {
        this.message = message;
        this.commitId = commitId;
        this.profile = profile;
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
}
