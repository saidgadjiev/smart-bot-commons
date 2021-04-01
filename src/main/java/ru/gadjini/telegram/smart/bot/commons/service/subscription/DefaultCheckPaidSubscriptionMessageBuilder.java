package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

public class DefaultCheckPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    private SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider;

    public DefaultCheckPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                      LocalisationService localisationService,
                                                      SubscriptionProperties paidSubscriptionProperties,
                                                      SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider) {
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.localisationService = localisationService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
        this.subscriptionTimeDeclensionProvider = subscriptionTimeDeclensionProvider;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        if (paidSubscription == null) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND,
                    new Object[]{
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice)},
                    locale
            );
        } else if (paidSubscription.isTrial()) {
            if (paidSubscription.isActive()) {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                                TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                                paidSubscriptionProperties.getPaymentBotName(),
                                NumberUtils.toString(minPrice)
                        },
                        locale);
            } else {
                return localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                                TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                                paidSubscriptionProperties.getPaymentBotName(),
                                NumberUtils.toString(minPrice)
                        },
                        locale);
            }
        } else if (paidSubscription.isActive()) {
            Period period = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_SUBSCRIPTION,
                    new Object[]{
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).months(period.getMonths()),
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice)},
                    locale);
        } else {
            Period period = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).months(period.getMonths()),
                            PaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice)
                    },
                    locale);
        }
    }
}
