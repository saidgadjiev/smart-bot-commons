package ru.gadjini.telegram.smart.bot.commons.service.currency;

import java.util.Map;

public class TelegramCurrencies {

    private final Map<String, TelegramCurrency> currencies;

    public TelegramCurrencies(Map<String, TelegramCurrency> currencies) {
        this.currencies = currencies;
    }

    public TelegramCurrency get(String current) {
        return currencies.get(current);
    }

    public Map<String, TelegramCurrency> getCurrencies() {
        return currencies;
    }
}
