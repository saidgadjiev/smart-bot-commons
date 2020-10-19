package ru.gadjini.telegram.smart.bot.commons.exception;

public class UnknownDownloadingUploadingException extends RuntimeException {
    public UnknownDownloadingUploadingException() {
    }

    public UnknownDownloadingUploadingException(String message) {
        super(message);
    }

    public UnknownDownloadingUploadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDownloadingUploadingException(Throwable cause) {
        super(cause);
    }

    public UnknownDownloadingUploadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
