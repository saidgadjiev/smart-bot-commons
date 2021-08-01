package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.apache.commons.lang3.StringUtils;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.Locale;
import java.util.Map;

public class FatherCheckPaidSubscriptionMessageBuilder {

    private CommonCheckPaidSubscriptionMessageBuilder commonCheckPaidSubscriptionMessageBuilder;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private Map<PaidSubscriptionTariffType, CheckPaidSubscriptionMessageBuilder> checkPaidSubscriptionMessageBuilderMap;

    public FatherCheckPaidSubscriptionMessageBuilder(CommonCheckPaidSubscriptionMessageBuilder commonCheckPaidSubscriptionMessageBuilder,
                                                     PaidSubscriptionPlanService paidSubscriptionPlanService,
                                                     Map<PaidSubscriptionTariffType,
                                                             CheckPaidSubscriptionMessageBuilder> checkPaidSubscriptionMessageBuilderMap) {
        this.commonCheckPaidSubscriptionMessageBuilder = commonCheckPaidSubscriptionMessageBuilder;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.checkPaidSubscriptionMessageBuilderMap = checkPaidSubscriptionMessageBuilderMap;
    }

    public String getMessage(PaidSubscription paidSubscription, Locale locale) {
        String message = commonCheckPaidSubscriptionMessageBuilder.getMessage(paidSubscription, locale);

        if (StringUtils.isBlank(message)) {
            PaidSubscriptionTariffType tariff = paidSubscriptionPlanService.getTariff(paidSubscription.getPlanId());

            return checkPaidSubscriptionMessageBuilderMap.get(tariff).getMessage(paidSubscription, locale);
        }

        return message;
    }
}
