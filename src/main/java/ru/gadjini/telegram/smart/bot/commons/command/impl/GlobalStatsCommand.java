package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

@Component
public class GlobalStatsCommand implements BotCommand {

    private UserService userService;

    private LocalisationService localisationService;

    private MessageService messageService;

    @Autowired
    public GlobalStatsCommand(UserService userService, LocalisationService localisationService, @TgMessageLimitsControl MessageService messageService) {
        this.userService = userService;
        this.localisationService = localisationService;
        this.messageService = messageService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        long activityToday = userService.countActiveUsers(0);
        long activityYesterday = userService.countActiveUsers(1);
        String statsMessage = localisationService.getMessage(
                MessagesProperties.MESSAGE_GLOBAL_STATS,
                new Object[]{activityToday, activityYesterday},
                locale
        );
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text(statsMessage)
                .parseMode(ParseMode.HTML).build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.GLOBAL_STATS;
    }
}
