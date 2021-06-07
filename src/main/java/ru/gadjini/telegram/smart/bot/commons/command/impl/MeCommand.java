package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidChannelSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.utils.TelegramLinkUtils;

@Component
public class MeCommand implements BotCommand, PaidChannelSubscriptionOptional {

    private MessageService messageService;

    @Autowired
    public MeCommand(@TgMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        StringBuilder text = new StringBuilder();
        text.append("ID - ").append(TelegramLinkUtils.userLink(message.getFrom().getId())).append("\n");
        text.append("Username - ").append(StringUtils.isNotBlank(message.getFrom().getUserName())
                ? TelegramLinkUtils.mention(message.getFrom().getUserName()) : "");

        messageService.sendMessage(SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(text.toString())
                .parseMode(ParseMode.HTML)
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.ME;
    }
}
