package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.job.BulkDistributionJob;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
public class EnableBulkDistributionCommand implements BotCommand {

    private BulkDistributionJob bulkDistributionJob;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public EnableBulkDistributionCommand(BulkDistributionJob bulkDistributionJob,
                                         @TgMessageLimitsControl MessageService messageService,
                                         UserService userService) {
        this.bulkDistributionJob = bulkDistributionJob;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        bulkDistributionJob.setDisable(!Boolean.parseBoolean(params[0]));
        messageService.sendMessage(SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(String.valueOf(!bulkDistributionJob.isDisable()))
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.BULK_DISTRIBUTION;
    }
}
