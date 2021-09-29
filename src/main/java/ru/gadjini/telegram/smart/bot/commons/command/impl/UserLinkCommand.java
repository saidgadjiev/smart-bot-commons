package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TelegramMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.utils.TelegramLinkUtils;

@Component
public class UserLinkCommand implements BotCommand {

    private MessageService messageService;

    @Autowired
    public UserLinkCommand(@TelegramMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getFrom().getId()))
                        .text(TelegramLinkUtils.userLink(message.getFrom().getId()))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.USER_LINK;
    }
}
