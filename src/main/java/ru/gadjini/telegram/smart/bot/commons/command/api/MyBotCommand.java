package ru.gadjini.telegram.smart.bot.commons.command.api;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;

public interface MyBotCommand {

    default void processNonCommandUpdate(Message message, String text) {
    }

    default boolean acceptNonCommandMessage(Message message) {
        return true;
    }

   default boolean accept(Message message) {
        return true;
    }

    default void cancel(long chatId, String queryId) {}
}
