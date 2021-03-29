package ru.gadjini.telegram.smart.bot.commons.service.command.message;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;

public class DefaultHelpCommandMessageBuilder implements HelpCommandMessageBuilder {

    private LocalisationService localisationService;

    private CommandMessageBuilder commandMessageBuilder;

    public DefaultHelpCommandMessageBuilder(LocalisationService localisationService, CommandMessageBuilder commandMessageBuilder) {
        this.localisationService = localisationService;
        this.commandMessageBuilder = commandMessageBuilder;
    }

    @Override
    public String getWelcomeMessage(Locale locale) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_HELP,
                new Object[]{commandMessageBuilder.getCommandsInfo(locale)},
                locale);
    }
}
