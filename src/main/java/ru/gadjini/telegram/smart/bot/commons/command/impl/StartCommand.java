package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

public class StartCommand implements BotCommand, NavigableBotCommand {

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private ReplyKeyboardService replyKeyboardService;

    public StartCommand(MessageService messageService, LocalisationService localisationService,
                        UserService userService, ReplyKeyboardService replyKeyboardService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean isChannelSubscriptionRequired() {
        return false;
    }

    @Override
    public boolean isPaidSubscriptionRequired() {
        return false;
    }

    @Override
    public boolean isChannelSubscriptionRequired(Message message) {
        return false;
    }

    @Override
    public boolean isPaidSubscriptionRequired(Message message) {
        return false;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_MAIN_MENU,
                                userService.getLocaleOrDefault(message.getFrom().getId())))
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getParentCommandName(long chatId) {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public void restore(TgMessage message) {
        Locale locale = userService.getLocaleOrDefault(message.getUser().getId());
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_MAIN_MENU, locale))
                .replyMarkup(replyKeyboardService.removeKeyboard(message.getChatId()))
                .parseMode(ParseMode.HTML).build());
    }

    @Override
    public ReplyKeyboard getKeyboard(long chatId) {
        return replyKeyboardService.mainMenuKeyboard(chatId, userService.getLocaleOrDefault((int) chatId));
    }

    @Override
    public String getMessage(long chatId) {
        return localisationService.getMessage(MessagesProperties.MESSAGE_MAIN_MENU, userService.getLocaleOrDefault((int) chatId));
    }
}
