package ru.gadjini.telegram.smart.bot.commons.service.queue;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.InlineKeyboardMarkup;

import java.util.Locale;

public interface QueueJobDelegate {

    default void init() {

    }

    WorkerTaskDelegate mapWorker(QueueItem queueItem);

    default void afterTaskCanceled(QueueItem queueItem) {

    }

    default void shutdown() {

    }

    default void currentTasksRemoved(int userId) {

    }

    default boolean isNeedUpdateMessageAfterCancel(QueueItem queueItem) {
        return true;
    }

    interface WorkerTaskDelegate {

        void execute() throws Exception;

        void cancel();

        default String getErrorCode(Throwable e) {
            return null;
        }

        String getWaitingMessage(QueueItem queueItem, Locale locale);

        InlineKeyboardMarkup getWaitingKeyboard(QueueItem queueItem, Locale locale);

        default void finish() {

        }

        default boolean shouldBeDeletedAfterCompleted() {
            return false;
        }

        default void unhandledException(Throwable e) {

        }
    }
}
