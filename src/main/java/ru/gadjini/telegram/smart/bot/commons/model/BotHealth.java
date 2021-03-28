package ru.gadjini.telegram.smart.bot.commons.model;

public class BotHealth {

    private String message;

    private String commitId;

    public BotHealth(String message, String commitId) {
        this.message = message;
        this.commitId = commitId;
    }

    public String getMessage() {
        return message;
    }

    public String getCommitId() {
        return commitId;
    }
}
