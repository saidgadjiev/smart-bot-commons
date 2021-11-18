package ru.gadjini.telegram.smart.bot.commons.event;

public class UserBlockedEvent {

    private long userId;

    public UserBlockedEvent(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
