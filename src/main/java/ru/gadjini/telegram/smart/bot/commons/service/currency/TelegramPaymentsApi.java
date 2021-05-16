package ru.gadjini.telegram.smart.bot.commons.service.currency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gadjini.telegram.smart.bot.commons.service.Jackson;

import java.util.Map;

@Service
public class TelegramPaymentsApi {

    private static final String CURRENCIES_URL = "https://core.telegram.org/bots/payments/currencies.json";

    private RestTemplate restTemplate;

    private Jackson json;

    @Autowired
    public TelegramPaymentsApi(RestTemplate restTemplate, Jackson json) {
        this.restTemplate = restTemplate;
        this.json = json;
    }

    public TelegramCurrencies getCurrencies() {
        ObjectNode result = restTemplate.getForObject(CURRENCIES_URL, ObjectNode.class);

        Map<String, TelegramCurrency> currencies = json.convertValue(result, new TypeReference<>() {
        });

        return new TelegramCurrencies(currencies);
    }
}
