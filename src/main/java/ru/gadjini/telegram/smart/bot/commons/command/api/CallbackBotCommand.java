package ru.gadjini.telegram.smart.bot.commons.command.api;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

public interface CallbackBotCommand extends MyBotCommand {

    String getName();

    /**
     */
    void processMessage(CallbackQuery callbackQuery, RequestParams requestParams);

    default void processNonCommandCallback(CallbackQuery callbackQuery, RequestParams requestParams) {

    }
}
