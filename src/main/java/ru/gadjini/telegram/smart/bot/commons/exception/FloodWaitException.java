package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodWaitException extends RuntimeException {

    public static final String FLOOD_WAIT_BASE_MESSAGE = "Flood wait";

    private long sleepTime;

    public FloodWaitException(long sleepTime) {
        super(FLOOD_WAIT_BASE_MESSAGE + " " + sleepTime);
    }

    public FloodWaitException(String message, long sleepTime) {
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

    public long getSleepTime() {
        return sleepTime;
    }
}
