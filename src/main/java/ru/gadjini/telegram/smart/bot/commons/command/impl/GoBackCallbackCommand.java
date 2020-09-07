package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CallbackCommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

@Component
public class GoBackCallbackCommand implements CallbackBotCommand {

    public static final String ARG_NAME = "gbcc";

    private CallbackCommandNavigator callbackCommandNavigator;

    @Autowired
    public void setCallbackCommandNavigator(CallbackCommandNavigator callbackCommandNavigator) {
        this.callbackCommandNavigator = callbackCommandNavigator;
    }

    @Override
    public String getName() {
        return CommandNames.GO_BACK_CALLBACK_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        String prevCommandName = requestParams.getString(ARG_NAME);

        callbackCommandNavigator.popTo(TgMessage.from(callbackQuery), prevCommandName, requestParams);
    }
}
