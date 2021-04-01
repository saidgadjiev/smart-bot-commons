package ru.gadjini.telegram.smart.bot.commons.service;

import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;

import java.util.Locale;

public class DummyCommandMessageBuilder implements CommandMessageBuilder {

    private LocalisationService localisationService;

    public DummyCommandMessageBuilder(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @Override
    public String getCommandsInfo(Locale locale) {
        return CommandParser.COMMAND_START_CHAR + CommandNames.START_COMMAND_NAME +
                " - " + localisationService.getMessage(MessagesProperties.START_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + CommandNames.TIME_COMMAND +
                " - " + localisationService.getMessage(MessagesProperties.BOT_TIME_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + CommandNames.LANGUAGE_COMMAND_NAME +
                " - " + localisationService.getMessage(MessagesProperties.LANGUAGE_COMMAND_DESCRIPTION, locale) +
                "\n" + CommandParser.COMMAND_START_CHAR + CommandNames.HELP_COMMAND +
                " - " + localisationService.getMessage(MessagesProperties.HELP_COMMAND_DESCRIPTION, locale);
    }
}
