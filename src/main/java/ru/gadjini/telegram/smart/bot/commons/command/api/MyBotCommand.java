package ru.gadjini.telegram.smart.bot.commons.command.api;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;

public interface MyBotCommand {

    default void processNonCommandUpdate(Message message, String text) {
    }

    default boolean acceptNonCommandMessage(Message message) {
        return message.hasText();
    }

   default boolean accept(Message message) {
        return message.hasText();
    }

    default void cancel(long chatId, String queryId) {}
}
