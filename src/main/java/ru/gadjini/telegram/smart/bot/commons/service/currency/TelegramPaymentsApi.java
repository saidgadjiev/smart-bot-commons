package ru.gadjini.telegram.smart.bot.commons.service.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramPaymentsApi {

    private static final String CURRENCIES_URL = "https://core.telegram.org/bots/payments/currencies.json";

    private RestTemplate restTemplate;

    @Autowired
    public TelegramPaymentsApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TelegramCurrencies getCurrencies() {
        return restTemplate.getForObject(CURRENCIES_URL, TelegramCurrencies.class);
    }
}
