package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

@Component
public class RmPaidSubscriptionCommand implements BotCommand {

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public RmPaidSubscriptionCommand(PaidSubscriptionRemoveService paidSubscriptionRemoveService,
                                     @TgMessageLimitsControl MessageService messageService, UserService userService) {
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        if (params.length == 0) {
            messageService.sendMessage(SendMessage.builder()
                    .chatId(String.valueOf(message.getChatId()))
                    .text("User id required")
                    .build());
        } else {
            try {
                long userId = Integer.parseInt(params[0]);
                int remove = paidSubscriptionRemoveService.removePaidSubscription(userId);
                if (remove == 1) {
                    messageService.sendMessage(SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text("User subscription removed")
                            .build());
                } else {
                    messageService.sendMessage(SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text("User subscription not found")
                            .build());
                }
            } catch (NumberFormatException e) {
                messageService.sendMessage(SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text("User id must be int")
                        .build());
            }
        }
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.RM_SUBSCRIPTION;
    }
}
