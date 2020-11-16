package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodControlException extends RuntimeException {

    private int sleepTime;

    public FloodControlException() {
    }

    public FloodControlException(String message, int sleepTime) {
        super(message + " " + sleepTime);
        this.sleepTime = sleepTime;
    }

    public FloodControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public FloodControlException(Throwable cause) {
        super(cause);
    }

    public FloodControlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getSleepTime() {
        return sleepTime;
    }
}
