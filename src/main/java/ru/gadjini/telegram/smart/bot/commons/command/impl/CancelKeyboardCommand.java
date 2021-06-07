package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidChannelSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class CancelKeyboardCommand implements KeyboardBotCommand, PaidChannelSubscriptionOptional {

    private CommandNavigator commandNavigator;

    private Set<String> names = new HashSet<>();

    @Autowired
    public CancelKeyboardCommand(LocalisationService localisationService) {
        for (Locale locale : localisationService.getSupportedLocales()) {
            this.names.add(localisationService.getMessage(MessagesProperties.CANCEL_COMMAND_DESCRIPTION, locale));
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
}
