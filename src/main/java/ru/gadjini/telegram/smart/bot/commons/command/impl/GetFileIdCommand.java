package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.model.MessageMedia;
import ru.gadjini.telegram.smart.bot.commons.service.MessageMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

@Component
public class GetFileIdCommand implements BotCommand, NavigableBotCommand {

    private MessageService messageService;

    private UserService userService;

    private MessageMediaService messageMediaService;

    private ReplyKeyboardService replyKeyboardService;

    @Autowired
    public GetFileIdCommand(@TgMessageLimitsControl MessageService messageService,
                            UserService userService, MessageMediaService messageMediaService,
                            @KeyboardHolder ReplyKeyboardService replyKeyboardService) {
        this.messageService = messageService;
        this.userService = userService;
        this.messageMediaService = messageMediaService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text("Send me a file")
                .replyMarkup(replyKeyboardService.goBackKeyboard(message.getFrom().getId(),
                        userService.getLocaleOrDefault(message.getFrom().getId())))
                .build());
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        MessageMedia media = messageMediaService.getMedia(message, Locale.getDefault());
        messageService.sendMessage(new SendMessage(String.valueOf(message.getChatId()), media.getFileId()));
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.GET_FILE_ID_COMMAND;
    }

    @Override
    public String getParentCommandName(long chatId) {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return CommandNames.GET_FILE_ID_COMMAND;
    }
}
