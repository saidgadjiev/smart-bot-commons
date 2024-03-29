package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;

public interface NavigableBotCommand {

    default boolean isChannelSubscriptionRequired(Message message) {
        return true;
    }

    default boolean isPaidSubscriptionRequired(Message message) {
        return true;
    }

    default boolean acceptNonCommandMessage(Message message) {
        return true;
    }

    default void processNonCommandUpdate(Message message, String text) {
    }

    String getParentCommandName(long chatId);

    String getHistoryName();

    default void restore(TgMessage message) {

    }

    default ReplyKeyboard getKeyboard(long chatId) {
        throw new UnsupportedOperationException();
    }

    default String getMessage(long chatId) {
        throw new UnsupportedOperationException();
    }

    default void leave(long chatId) {

    }

    default void cancel(long chatId, String queryId) {
    }

    default boolean setPrevCommand(long chatId, String prevCommand) {
        return false;
    }

    default boolean canLeave(long chatId) {
        return true;
    }
}
