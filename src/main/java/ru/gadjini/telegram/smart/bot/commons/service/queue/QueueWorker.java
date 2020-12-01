package ru.gadjini.telegram.smart.bot.commons.service.queue;

public interface QueueWorker {

    void execute() throws Exception;

    default void cancel() {

    }

    default void finish() {

    }

    default void unhandledException(Throwable e) {

    }
}
