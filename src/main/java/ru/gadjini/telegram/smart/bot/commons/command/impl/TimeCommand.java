package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeCommand implements BotCommand {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss '<i>'z'</i>'");

    private MessageService messageService;

    @Autowired
    public TimeCommand(@TgMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.TIME_COMMAND;
    }
}
