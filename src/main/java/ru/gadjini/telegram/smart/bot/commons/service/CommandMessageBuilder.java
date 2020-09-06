package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;

import java.util.Collection;
import java.util.Locale;

@Service
public class CommandMessageBuilder {

    private Collection<BotCommand> botCommands;

    @Autowired
    public void setBotCommands(Collection<BotCommand> botCommands) {
        this.botCommands = botCommands;
    }

    public String getCommandsInfo(Locale locale) {
        StringBuilder commands = new StringBuilder();

        for (BotCommand botCommand: botCommands) {
            String commandDescription = botCommand.getCommandDescription(locale);

            if (StringUtils.isNotBlank(commandDescription)) {
                commands.append(commandDescription);
            }
        }

        return commands.toString();
    }
}
