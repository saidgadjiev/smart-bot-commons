package ru.gadjini.telegram.smart.bot.commons.command.api;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;

import java.util.Locale;

public interface BotCommand {
    String COMMAND_INIT_CHARACTER = "/";

    void processMessage(Message message, String[] params);

    String getCommandIdentifier();

    default String getCommandDescription(Locale locale) {
        return null;
    }
}
