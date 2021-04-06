package ru.gadjini.telegram.smart.bot.commons.service.currency;

import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;

public class TelegramCurrencyConverter {

    private TelegramCurrencies telegramCurrencies;

    public TelegramCurrencyConverter(TelegramCurrencies telegramCurrencies) {
        this.telegramCurrencies = telegramCurrencies;
    }

    public double convertToRub(double usd) {
        double usdRubCurrency = (double) telegramCurrencies.getRub().getMinAmount() / TgConstants.PAYMENTS_AMOUNT_FACTOR;

        return usdRubCurrency * usd;
    }
}
