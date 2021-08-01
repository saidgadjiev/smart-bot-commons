package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;

import java.util.Locale;

public class DefaultCheckFlexibleTariffPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    public DefaultCheckFlexibleTariffPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                                    SubscriptionProperties paidSubscriptionProperties,
                                                                    PaidSubscriptionPlanService paidSubscriptionPlanService) {
        this.localisationService = localisationService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        if (paidSubscription.isSubscriptionIntervalActive()) {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FLEXIBLE_SUBSCRIPTION,
                    new Object[]{
                            JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval()),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice, 2)},
                    locale);
        } else {
            return localisationService.getMessage(
                    MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate()),
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getPurchaseDate()),
                            paidSubscriptionProperties.getPaymentBotName(),
                            NumberUtils.toString(minPrice, 2)
                    },
                    locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}
