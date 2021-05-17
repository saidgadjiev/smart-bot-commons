package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

@Component
public class RefreshAllSubscriptionsCommand implements BotCommand, PaidSubscriptionOptional {

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    private MessageService messageService;

    private SubscriptionProperties subscriptionProperties;

    private UserService userService;

    @Autowired
    public RefreshAllSubscriptionsCommand(PaidSubscriptionRemoveService paidSubscriptionRemoveService,
                                          @TgMessageLimitsControl MessageService messageService,
                                          SubscriptionProperties subscriptionProperties,
                                          UserService userService) {
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
        this.messageService = messageService;
        this.subscriptionProperties = subscriptionProperties;
        this.userService = userService;
    }

    @Override
    public boolean accept(Message message) {
        return subscriptionProperties.isCheckPaidSubscription()
                || userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        paidSubscriptionRemoveService.refreshAllPaidSubscriptions();
        messageService.sendMessage(SendMessage.builder()
                .text("Refreshed")
                .chatId(String.valueOf(message.getChatId()))
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.REFRESH_SUBSCRIPTIONS;
    }
}
