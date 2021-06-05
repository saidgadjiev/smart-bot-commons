package ru.gadjini.telegram.smart.bot.commons.service.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelegramCurrencyConverterFactory {

    private TelegramPaymentsApi telegramPaymentsApi;

    @Autowired
    public TelegramCurrencyConverterFactory(TelegramPaymentsApi telegramPaymentsApi) {
        this.telegramPaymentsApi = telegramPaymentsApi;
    }

    public TelegramCurrencyConverter createConverter() {
        TelegramCurrencies currencies = telegramPaymentsApi.getCurrencies();
        addCustomCurrencies(currencies);

        return new TelegramCurrencyConverter(currencies);
    }

    private void addCustomCurrencies(TelegramCurrencies telegramCurrencies) {
        TelegramCurrency telegramCurrency = new TelegramCurrency();
        telegramCurrency.setMinAmount(100);
        telegramCurrencies.getCurrencies().put("USDT", telegramCurrency);
    }
}
