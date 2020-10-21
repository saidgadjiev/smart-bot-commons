package ru.gadjini.telegram.smart.bot.commons.exception;

public class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}
