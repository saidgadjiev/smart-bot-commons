package ru.gadjini.telegram.smart.bot.commons.service.currency;

import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;

public class TelegramCurrencyConverter {

    private TelegramCurrencies telegramCurrencies;

    public TelegramCurrencyConverter(TelegramCurrencies telegramCurrencies) {
        this.telegramCurrencies = telegramCurrencies;
    }

    public TelegramCurrencies getTelegramCurrencies() {
        return telegramCurrencies;
    }

    public double convertTo(double usd, String targetCurrency) {
        if (targetCurrency.equalsIgnoreCase(TgConstants.USD_CURRENCY)) {
            return usd;
        }
        double converted = (double) telegramCurrencies.get(targetCurrency).getMinAmount() / TgConstants.PAYMENTS_AMOUNT_FACTOR;

        return converted * usd;
    }
}
