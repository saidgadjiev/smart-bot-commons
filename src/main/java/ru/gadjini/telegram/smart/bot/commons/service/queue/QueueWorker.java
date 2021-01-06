package ru.gadjini.telegram.smart.bot.commons.service.queue;

import ru.gadjini.telegram.smart.bot.commons.service.localisation.ErrorCode;

public interface QueueWorker {

    void execute() throws Exception;

    default void cancel(boolean canceledByUser) {

    }

    default void finish() {

    }

    default void unhandledException(Throwable e) {

    }

    default ErrorCode getErrorCode(Throwable e) {
        return ErrorCode.EMPTY;
    }
}
