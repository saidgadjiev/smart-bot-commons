package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.method.send.HtmlMessage;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Message;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class HelpCommand implements KeyboardBotCommand, BotCommand {

    private final MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private Set<String> names = new HashSet<>();

    private CommandMessageBuilder commandMessageBuilder;

    @Autowired
    public HelpCommand(@Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService,
                       UserService userService, CommandMessageBuilder commandMessageBuilder) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.commandMessageBuilder = commandMessageBuilder;
        for (Locale locale : localisationService.getSupportedLocales()) {
            String message = localisationService.getMessage(MessagesProperties.HELP_COMMAND_NAME, locale, null);
            if (message != null) {
                this.names.add(message);
            }
        }
    }

    @Override
    public void processMessage(Message message, String[] params) {
        processMessage(message, (String) null);
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.HELP_COMMAND;
    }

    @Override
    public boolean canHandle(long chatId, String command) {
        return names.contains(command);
    }

    @Override
    public boolean processMessage(Message message, String text) {
        sendHelpMessage(message.getFrom().getId(), userService.getLocaleOrDefault(message.getFrom().getId()));

        return false;
    }

    private void sendHelpMessage(int userId, Locale locale) {
        messageService.sendMessage(
                new HtmlMessage((long) userId, localisationService.getMessage(MessagesProperties.MESSAGE_HELP,
                        new Object[]{commandMessageBuilder.getCommandsInfo(locale)},
                        locale)));
    }
}
