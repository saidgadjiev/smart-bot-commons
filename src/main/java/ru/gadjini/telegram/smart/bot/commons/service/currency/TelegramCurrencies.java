package ru.gadjini.telegram.smart.bot.commons.service.currency;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelegramCurrencies {

    @JsonProperty("RUB")
    private TelegramCurrency rub;

    public TelegramCurrency getRub() {
        return rub;
    }

    public void setRub(TelegramCurrency rub) {
        this.rub = rub;
    }
}
