package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidChannelSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import javax.annotation.PostConstruct;

@Component
public class BuildInfoCommand implements BotCommand, PaidChannelSubscriptionOptional {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfoCommand.class);

    @Value("${git.commit.id}")
    private String commitId;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public BuildInfoCommand(@TgMessageLimitsControl MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Git commit id({})", commitId);
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(
                SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                        .text(commitId)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.BUILD_INFO;
    }
}
