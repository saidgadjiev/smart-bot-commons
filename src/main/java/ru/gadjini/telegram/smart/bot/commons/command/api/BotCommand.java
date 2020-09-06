package ru.gadjini.telegram.smart.bot.commons.command.api;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;

public interface BotCommand {
    String COMMAND_INIT_CHARACTER = "/";

    void processMessage(Message message, String[] params);

    String getCommandIdentifier();
}
