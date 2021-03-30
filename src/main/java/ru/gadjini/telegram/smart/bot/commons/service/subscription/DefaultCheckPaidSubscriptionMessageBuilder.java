package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;

public class DefaultCheckPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    public DefaultCheckPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                      LocalisationService localisationService,
                                                      SubscriptionProperties paidSubscriptionProperties) {
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.localisationService = localisationService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        if (paidSubscription == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND,
                    new Object[]{
                            paidSubscriptionProperties.getPaymentBotName(),
                            String.valueOf(minPrice)},
                    locale
            );
        } else if (paidSubscription.isTrial()) {
            if (paidSubscription.isActive()) {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                                paidSubscriptionProperties.getPaymentBotName(),
                                String.valueOf(minPrice)
                        },
                        locale);
            } else {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                                paidSubscriptionProperties.getPaymentBotName(),
                                String.valueOf(minPrice)
                        },
                        locale);
            }
        } else if (paidSubscription.isActive()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            paidSubscriptionProperties.getPaymentBotName(),
                            String.valueOf(minPrice)},
                    locale);
        } else {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndDate()),
                            PaidSubscriptionService.PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            paidSubscriptionProperties.getPaymentBotName(),
                            String.valueOf(minPrice)
                    },
                    locale);
        }
    }
}
