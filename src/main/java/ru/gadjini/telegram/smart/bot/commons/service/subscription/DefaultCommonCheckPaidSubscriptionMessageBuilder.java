package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;

public class DefaultCommonCheckPaidSubscriptionMessageBuilder implements CommonCheckPaidSubscriptionMessageBuilder {

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private LocalisationService localisationService;

    private PaidSubscriptionMessageBuilder paidSubscriptionMessageBuilder;

    private FixedTariffPaidSubscriptionService fixedTariffPaidSubscriptionService;

    public DefaultCommonCheckPaidSubscriptionMessageBuilder(PaidSubscriptionPlanService paidSubscriptionPlanService,
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

        if (paidSubscription == null) {
            return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                    MessagesProperties.MESSAGE_SUBSCRIPTION_NOT_FOUND,
                    locale
            ))
                    .withSubscriptionInstructions(minPrice)
                    .buildMessage(locale);
        } else if (paidSubscription.isTrial()) {
            if (fixedTariffPaidSubscriptionService.isSubscriptionPeriodActive(paidSubscription)) {
                return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt())
                        },
                        locale)
                )
                        .withSubscriptionFor()
                        .withUtcTime()
                        .withSubscriptionInstructions(minPrice)
                        .buildMessage(locale);
            } else {
                return paidSubscriptionMessageBuilder.builder(localisationService.getMessage(
                        MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_EXPIRED,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(paidSubscription.getEndAt())
                        },
                        locale)
                )
                        .withSubscriptionFor()
                        .withUtcTime()
                        .withSubscriptionInstructions(minPrice)
                        .buildMessage(locale);
            }
        } else {
            return null;
        }
    }
}
