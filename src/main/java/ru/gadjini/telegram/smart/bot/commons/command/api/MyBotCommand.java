package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MyBotCommand {

    default boolean acceptNonCommandMessage(Message message) {
        return true;
    }

    default boolean accept(Message message) {
        return true;
    }

    default void cancel(long chatId, String queryId) {
    }
}
