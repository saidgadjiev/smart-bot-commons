package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodWaitException extends RuntimeException {

    private int sleepTime;

    public FloodWaitException() {
    }

    public FloodWaitException(String message, int sleepTime) {
        super(message + " " + sleepTime);
        this.sleepTime = sleepTime;
    }

    public FloodWaitException(String message, Throwable cause) {
        super(message, cause);
    }

    public FloodWaitException(Throwable cause) {
        super(cause);
    }

    public FloodWaitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getSleepTime() {
        return sleepTime;
    }
}
