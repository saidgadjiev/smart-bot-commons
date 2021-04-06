package ru.gadjini.telegram.smart.bot.commons.service.currency;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelegramCurrency {

    @JsonProperty("min_amount")
    private int minAmount;

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }
}
