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

        return new TelegramCurrencyConverter(currencies);
    }
}
