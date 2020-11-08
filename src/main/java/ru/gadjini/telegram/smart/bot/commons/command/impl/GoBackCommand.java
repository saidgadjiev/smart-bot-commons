package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class GoBackCommand implements KeyboardBotCommand, BotCommand {

    private CommandNavigator commandNavigator;

    private Set<String> names = new HashSet<>();

    @Autowired
    public GoBackCommand(LocalisationService localisationService) {
        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale));
        }
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        commandNavigator.pop(TgMessage.from(message));

        return false;
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.GO_BACK;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        commandNavigator.pop(TgMessage.from(message));
    }
}
