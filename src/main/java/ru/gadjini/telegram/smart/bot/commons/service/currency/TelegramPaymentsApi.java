package ru.gadjini.telegram.smart.bot.commons.service.currency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TelegramPaymentsApi {

    private static final String CURRENCIES_URL = "https://core.telegram.org/bots/payments/currencies.json";

    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public TelegramPaymentsApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public TelegramCurrencies getCurrencies() {
        ObjectNode result = restTemplate.getForObject(CURRENCIES_URL, ObjectNode.class);

        Map<String, TelegramCurrency> currencies = objectMapper.convertValue(result, new TypeReference<>() {
        });

        return new TelegramCurrencies(currencies);
    }
}
