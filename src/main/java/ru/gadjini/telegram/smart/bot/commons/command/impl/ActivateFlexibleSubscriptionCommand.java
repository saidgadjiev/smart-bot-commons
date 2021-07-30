package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.FlexibleTariffPaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.util.Locale;

@Component
public class ActivateFlexibleSubscriptionCommand implements CallbackBotCommand {

    private FlexibleTariffPaidSubscriptionService flexibleTariffPaidSubscriptionService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private BotProperties botProperties;

    @Autowired
    public ActivateFlexibleSubscriptionCommand(FlexibleTariffPaidSubscriptionService flexibleTariffPaidSubscriptionService,
                                               @TgMessageLimitsControl MessageService messageService, LocalisationService localisationService,
                                               UserService userService, BotProperties botProperties) {
        this.flexibleTariffPaidSubscriptionService = flexibleTariffPaidSubscriptionService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.botProperties = botProperties;
    }

    @Override
    public void processCallbackQuery(CallbackQuery callbackQuery, RequestParams requestParams) {
        PaidSubscription paidSubscription = flexibleTariffPaidSubscriptionService.activateSubscriptionDay(
                botProperties.getName(), callbackQuery.getFrom().getId()
        );
        Locale locale = userService.getLocaleOrDefault(callbackQuery.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(callbackQuery.getFrom().getId()))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_DAY_ACTIVATED,
                                new Object[]{JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval())}, locale))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
        messageService.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
    }

    @Override
    public String getName() {
        return CommandNames.ACTIVATE_FLEXIBLE_SUBSCRIPTION;
    }
}
