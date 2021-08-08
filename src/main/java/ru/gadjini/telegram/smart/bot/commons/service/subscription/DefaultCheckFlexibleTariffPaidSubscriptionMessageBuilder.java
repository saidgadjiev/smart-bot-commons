package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.util.Locale;

public class DefaultCheckFlexibleTariffPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder;

    public DefaultCheckFlexibleTariffPaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                                                    PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                                    PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder) {
        this.localisationService = localisationService;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.paidSubscriptionMessageBuilder = paidSubscriptionMessageBuilder;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        if (paidSubscription.isSubscriptionIntervalActive()) {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FLEXIBLE_SUBSCRIPTION,
                    new Object[]{
                            JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval()),
                    },
                    locale)
            )
                    .withSubscriptionFor()
                    .withPurchaseDate(paidSubscription.getPurchaseDate())
                    .withSubscriptionInstructions(minPrice)
                    .buildMessage(locale);
        } else {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_FLEXIBLE_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getZonedEndDate())
                    },
                    locale)
            )
                    .withSubscriptionFor()
                    .withPurchaseDate(paidSubscription.getPurchaseDate())
                    .withSubscriptionInstructions(minPrice)
                    .buildMessage(locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}
