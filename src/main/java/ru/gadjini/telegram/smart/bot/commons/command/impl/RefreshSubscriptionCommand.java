package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

@Component
public class RefreshSubscriptionCommand implements BotCommand {

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    private MessageService messageService;

    @Autowired
    public RefreshSubscriptionCommand(PaidSubscriptionRemoveService paidSubscriptionRemoveService,
                                      @TgMessageLimitsControl MessageService messageService) {
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
        this.messageService = messageService;
    }

    @Override
    public boolean isPaidSubscriptionRequired() {
        return false;
    }

    @Override
    public boolean isPaidSubscriptionRequired(Message message) {
        return false;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        paidSubscriptionRemoveService.refreshPaidSubscription(message.getFrom().getId());
        messageService.sendMessage(SendMessage.builder()
                .text("Subscription refreshed")
                .chatId(String.valueOf(message.getChatId()))
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.REFRESH_SUBSCRIPTION;
    }
}
