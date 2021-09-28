package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.Locale;

public class DefaultCheckFixedTariffPaidSubscriptionMessageBuilder implements CheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder;

    private FixedTariffPaidSubscriptionService fixedTariffPaidSubscriptionService;

    public DefaultCheckFixedTariffPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                                 LocalisationService localisationService,
                                                                 PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder,
                                                                 FixedTariffPaidSubscriptionService fixedTariffPaidSubscriptionService) {
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.localisationService = localisationService;
        this.paidSubscriptionMessageBuilder = paidSubscriptionMessageBuilder;
        this.fixedTariffPaidSubscriptionService = fixedTariffPaidSubscriptionService;
    }

    @Override
    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        double minPrice = paidSubscriptionPlanService.getMinPrice();

        if (fixedTariffPaidSubscriptionService.isSubscriptionPeriodActive(paidSubscription)) {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_ACTIVE_FIXED_SUBSCRIPTION,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt()),
                    }, locale)
            )
                    .withPaidSubscriptionAccesses()
                    .withSubscriptionFor()
                    .withPurchaseDate(paidSubscription.getPurchasedAt())
                    .withUtcTime()
                    .withSubscriptionInstructions(minPrice)
                    .buildMessage(locale);
        } else {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_FIXED_SUBSCRIPTION_EXPIRED,
                    new Object[]{
                            FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt())
                    },
                    locale)
            )
                    .withPaidSubscriptionFeatures()
                    .withSubscriptionFor()
                    .withPurchaseDate(paidSubscription.getPurchasedAt())
                    .withSubscriptionInstructions(minPrice)
                    .buildMessage(locale);
        }
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }
}
