package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FatherCheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FatherPaidSubscriptionService;

import java.util.Locale;
import java.util.Objects;

@Component
public class CheckPaidSubscriptionCommand implements BotCommand, PaidSubscriptionOptional {

    private MessageService messageService;

    private UserService userService;

    private FatherPaidSubscriptionService paidSubscriptionService;

    private SubscriptionProperties paidSubscriptionProperties;

    private FatherCheckPaidSubscriptionMessageBuilder checkPaidSubscriptionMessageBuilder;

    private BotProperties botProperties;

    @Autowired
    public CheckPaidSubscriptionCommand(@TgMessageLimitsControl MessageService messageService,
                                        UserService userService,
                                        FatherPaidSubscriptionService paidSubscriptionService,
                                        SubscriptionProperties paidSubscriptionProperties,
                                        FatherCheckPaidSubscriptionMessageBuilder checkPaidSubscriptionMessageBuilder,
                                        BotProperties botProperties) {
        this.messageService = messageService;
        this.userService = userService;
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
        this.checkPaidSubscriptionMessageBuilder = checkPaidSubscriptionMessageBuilder;
        this.botProperties = botProperties;
    }

    @Override
    public boolean accept(Message message) {
        return paidSubscriptionProperties.isCheckPaidSubscription()
                || Objects.equals(botProperties.getName(), paidSubscriptionProperties.getPaymentBotName());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        PaidSubscription paidSubscription;
        if (params.length > 0 && userService.isAdmin(message.getFrom().getId())) {
            paidSubscription = paidSubscriptionService.getSubscription(Long.parseLong(params[0]));
        } else {
            paidSubscription = paidSubscriptionService.getSubscription(message.getFrom().getId());
        }
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(checkPaidSubscriptionMessageBuilder.getMessage(paidSubscription, locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.SUBSCRIPTION;
    }
}
