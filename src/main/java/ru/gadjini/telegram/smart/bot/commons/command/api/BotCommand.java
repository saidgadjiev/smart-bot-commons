package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommand extends MyBotCommand {
    String COMMAND_INIT_CHARACTER = "/";

    void processMessage(Message message, String[] params);

    String getCommandIdentifier();
}
