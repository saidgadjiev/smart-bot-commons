package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.property.PaidSubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;

import java.util.Locale;

@Component
public class CheckPaidSubscriptionCommand implements BotCommand {

    private MessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private PaidSubscriptionService paidSubscriptionService;

    private PaidSubscriptionProperties paidSubscriptionProperties;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    @Autowired
    public CheckPaidSubscriptionCommand(@TgMessageLimitsControl MessageService messageService,
                                        LocalisationService localisationService, UserService userService,
                                        PaidSubscriptionService paidSubscriptionService,
                                        PaidSubscriptionProperties paidSubscriptionProperties,
                                        PaidSubscriptionPlanService paidSubscriptionPlanService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.paidSubscriptionService = paidSubscriptionService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
    }

    @Override
    public boolean accept(Message message) {
        return paidSubscriptionProperties.isCheckPaidSubscription();
    }

    @Override
    public void processMessage(Message message, String[] params) {
        PaidSubscription paidSubscription = paidSubscriptionService.getSubscription(message.getFrom().getId());
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .text(getSubscriptionMessage(paidSubscription, locale))
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.SUBSCRIPTION;
    }

    private String getSubscriptionMessage(PaidSubscription paidSubscription, Locale locale) {
        PaidSubscriptionPlan activePlan = paidSubscriptionPlanService.getActivePlan();

        if (paidSubscription == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND,
                    new Object[]{String.valueOf(activePlan.getPrice())},
                    locale
            );
        } else if (paidSubscription.isTrial()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            String.valueOf(activePlan.getPrice())
                    },
                    locale);
        } else if (paidSubscription.isActive()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            String.valueOf(activePlan.getPrice())},
                    locale);
        } else {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            String.valueOf(activePlan.getPrice())
                    },
                    locale);
        }
    }
}
