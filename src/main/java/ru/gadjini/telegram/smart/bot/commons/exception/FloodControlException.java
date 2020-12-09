package ru.gadjini.telegram.smart.bot.commons.exception;

public class FloodControlException extends RuntimeException {

    public static final String FLOOD_WAIT_BASE_MESSAGE = "Flood wait";

    private long sleepTime;

    public FloodControlException(long sleepTime, boolean floodWait) {
        super((floodWait ? FLOOD_WAIT_BASE_MESSAGE + " " : "Flood control ") + sleepTime);
        this.sleepTime = sleepTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }
}
