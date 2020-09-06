package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

@Component
public class CallbackDelegate implements CallbackBotCommand {

    public static final String ARG_NAME = "cldg";

    private CommandExecutor commandExecutor;

    @Autowired
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String getName() {
        return CommandNames.CALLBACK_DELEGATE_COMMAND_NAME;
    }

    @Override
    public void processMessage(CallbackQuery callbackQuery, RequestParams requestParams) {
        String delegateCommand = requestParams.getString(ARG_NAME);
        CallbackBotCommand callbackCommand = commandExecutor.getCallbackCommand(delegateCommand);

        callbackCommand.processNonCommandCallback(callbackQuery, requestParams);
    }
}
