package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.declension.SubscriptionTimeDeclensionProvider;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

public class DefaultCheckFixedTariffPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    private SubscriptionTimeDeclensionProvider subscriptionTimeDeclensionProvider;

    public DefaultCheckFixedTariffPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
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

        if (paidSubscription.isActive()) {
            Period period = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FIXED_SUBSCRIPTION,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).localize(period),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice, 2)},
                    locale);
        } else {
            Period period = paidSubscriptionPlanService.getPlanPeriod(paidSubscription.getPlanId());
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            subscriptionTimeDeclensionProvider.getService(locale.getLanguage()).localize(period),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC)),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice, 2)
                    },
                    locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }
}
