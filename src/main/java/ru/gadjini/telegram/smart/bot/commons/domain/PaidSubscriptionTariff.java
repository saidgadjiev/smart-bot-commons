package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

public class PaidSubscriptionTariff {

    public static final String TARIFF_TYPE = "tariff_type";

    private PaidSubscriptionTariffType tariffType;

    public PaidSubscriptionTariffType getTariffType() {
        return tariffType;
    }

    public void setTariffType(PaidSubscriptionTariffType tariffType) {
        this.tariffType = tariffType;
    }
}
