package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;

@Component
public class CancelCommand implements CallbackBotCommand {

    private CommandExecutor commandExecutor;

    @Autowired
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String getName() {
        return CommandNames.CANCEL_COMMAND_NAME;
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        commandExecutor.cancelCommand(callbackQuery.getMessage().getChatId(), callbackQuery.getId());
    }
}
