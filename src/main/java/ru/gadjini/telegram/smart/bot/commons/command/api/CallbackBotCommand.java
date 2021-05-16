package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

public interface CallbackBotCommand extends SmartBotCommand {

    String getName();

    /**
     *
     */
    default void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {

    }

    default void processNonCommandCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {

    }
}
