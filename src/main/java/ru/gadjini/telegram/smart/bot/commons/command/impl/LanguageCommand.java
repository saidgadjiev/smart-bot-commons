package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidChannelSubscriptionFullyOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class LanguageCommand implements KeyboardBotCommand, BotCommand, NavigableBotCommand, PaidChannelSubscriptionFullyOptional {

    private Set<String> names = new HashSet<>();

    private final LocalisationService localisationService;

    private MessageService messageService;

    private UserService userService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    @Autowired
    public LanguageCommand(LocalisationService localisationService, @TgMessageLimitsControl MessageService messageService,
                           UserService userService, @KeyboardHolder ReplyKeyboardService replyKeyboardService) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.userService = userService;
        this.replyKeyboardService = replyKeyboardService;
        for (Locale locale : localisationService.getSupportedLocales()) {
            String message = localisationService.getMessage(MessagesProperties.LANGUAGE_COMMAND_NAME, locale, null);
            if (message != null) {
                this.names.add(message);
            }
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
    public void processMessage(Message message, String[] params) {
        processMessage(message, (String) null);
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.LANGUAGE_COMMAND_NAME;
    }

    @Override
    public boolean processMessage(Message message, String text) {
        processMessage0(message.getChatId(), message.getFrom().getId());

        return true;
    }

    private void processMessage0(long chatId, long userId) {
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(chatId))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_CHOOSE_LANGUAGE, locale))
                .replyMarkup(replyKeyboardService.languageKeyboard(chatId, locale)).build());
    }

    @Override
    public String getParentCommandName(long chatId) {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.LANGUAGE_COMMAND_NAME;
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        text = text.toLowerCase();
        for (Locale locale : localisationService.getSupportedLocales()) {
            if (text.equals(locale.getDisplayLanguage(locale).toLowerCase())) {
                changeLocale(message, locale);
                return;
            }
        }
    }

    private void changeLocale(Message message, Locale locale) {
        userService.changeLocale(message.getFrom().getId(), locale);
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_LANGUAGE_SELECTED, locale))
                        .replyMarkup(replyKeyboardService.mainMenuKeyboard(message.getChatId(), locale))
                        .build()
        );
        commandNavigator.silentPop(message.getChatId());
    }
}
