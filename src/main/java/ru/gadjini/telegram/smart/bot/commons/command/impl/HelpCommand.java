package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidChannelSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.HelpCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class HelpCommand implements KeyboardBotCommand, BotCommand, PaidChannelSubscriptionOptional {

    private final MessageService messageService;

    private UserService userService;

    private Set<String> names = new HashSet<>();

    private HelpCommandMessageBuilder helpCommandMessageBuilder;

    @Autowired
    public HelpCommand(@TgMessageLimitsControl MessageService messageService, LocalisationService localisationService,
                       UserService userService, HelpCommandMessageBuilder helpCommandMessageBuilder) {
        this.messageService = messageService;
        this.userService = userService;
        this.helpCommandMessageBuilder = helpCommandMessageBuilder;
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

    private void sendHelpMessage(long userId, Locale locale) {
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(userId))
                        .text(helpCommandMessageBuilder.getWelcomeMessage(locale))
                        .parseMode(ParseMode.HTML).build());
    }
}
