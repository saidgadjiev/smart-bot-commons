package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

import java.util.Objects;

@Component
public class RefreshSubscriptionCommand implements BotCommand {

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    private MessageService messageService;

    private SubscriptionProperties subscriptionProperties;

    private LocalisationService localisationService;

    private BotProperties botProperties;

    private UserService userService;

    @Autowired
    public RefreshSubscriptionCommand(PaidSubscriptionRemoveService paidSubscriptionRemoveService,
                                      @TgMessageLimitsControl MessageService messageService,
                                      SubscriptionProperties subscriptionProperties,
                                      LocalisationService localisationService, BotProperties botProperties,
                                      UserService userService) {
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
        this.messageService = messageService;
        this.subscriptionProperties = subscriptionProperties;
        this.localisationService = localisationService;
        this.botProperties = botProperties;
        this.userService = userService;
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
    public boolean accept(Message message) {
        return subscriptionProperties.isCheckPaidSubscription()
                || Objects.equals(botProperties.getName(), subscriptionProperties.getPaymentBotName());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        paidSubscriptionRemoveService.refreshPaidSubscription(message.getFrom().getId());
        messageService.sendMessage(SendMessage.builder()
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_SUBSCRIPTION_REFRESHED,
                        userService.getLocaleOrDefault(message.getFrom().getId())))
                .chatId(String.valueOf(message.getChatId()))
                .build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.REFRESH_SUBSCRIPTION;
    }
}
