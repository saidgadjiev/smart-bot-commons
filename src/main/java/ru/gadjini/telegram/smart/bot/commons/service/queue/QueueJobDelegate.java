package ru.gadjini.telegram.smart.bot.commons.service.queue;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.InlineKeyboardMarkup;

import java.util.Locale;

public interface QueueJobDelegate {

    void init();

    WorkerTaskDelegate mapWorker(QueueItem queueItem);

    void afterTaskCanceled(int id);

    void shutdown();

    interface WorkerTaskDelegate {

        void execute() throws Exception;

        void cancel();

        String getErrorCode(Throwable e);

        String getWaitingMessage(QueueItem queueItem, Locale locale);

        InlineKeyboardMarkup getWaitingKeyboard(QueueItem queueItem, Locale locale);

        default void finish() {

        }
    }
}
