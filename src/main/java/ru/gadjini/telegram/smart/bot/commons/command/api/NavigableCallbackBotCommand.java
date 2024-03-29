package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

public interface NavigableCallbackBotCommand {

    String getName();

    default void restore(TgMessage tgMessage, ReplyKeyboard replyKeyboard, RequestParams requestParams) {

    }

    default void leave(long chatId) {

    }

    default boolean isAcquireKeyboard() {
        return false;
    }
}
