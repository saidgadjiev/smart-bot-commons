package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

public class DefaultCommonCheckPaidSubscriptionMessageBuilder implements CommonCheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    public DefaultCommonCheckPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
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
                            NumberUtils.toString(minPrice, 2)},
                    locale
            );
        } else if (paidSubscription.isTrial()) {
            if (paidSubscription.isActive()) {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                                TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                                paidSubscriptionProperties.getPaymentBotName(),
                                NumberUtils.toString(minPrice, 2)
                        },
                        locale);
            } else {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                                TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                                paidSubscriptionProperties.getPaymentBotName(),
                                NumberUtils.toString(minPrice, 2)
                        },
                        locale);
            }
        } else {
            return null;
        }
    }
}
