package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

public interface CallbackBotCommand extends MyBotCommand {

    String getName();

    /**
     *
     */
    default void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {

    }

    default void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {

    }
}
