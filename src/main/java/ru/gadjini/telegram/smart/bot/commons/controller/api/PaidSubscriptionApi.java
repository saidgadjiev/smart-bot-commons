package ru.gadjini.telegram.smart.bot.commons.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.telegram.smart.bot.commons.model.RefreshPaidSubscriptionRequest;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;

@Component
public class PaidSubscriptionApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaidSubscriptionApi.class);

    private AuthProperties authProperties;

    private RestTemplate restTemplate;

    @Autowired
    public PaidSubscriptionApi(AuthProperties authProperties, RestTemplate restTemplate) {
        this.authProperties = authProperties;
        this.restTemplate = restTemplate;
    }

    public void refreshPaidSubscription(String server, long userId, String sourceBotName) {
        try {
            HttpEntity<Object> request = new HttpEntity<>(createRequest(sourceBotName), authHeaders());
            ResponseEntity<Void> response = restTemplate.postForEntity(buildUrl(server, userId), request, Void.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Refresh paid subscription failed({}, {}, {})", server, response.getStatusCodeValue(), userId);
            }
        } catch (Throwable e) {
            LOGGER.error("Refresh paid subscription failed(" + server + ", " + userId + ")\n" + e.getMessage(), e);
        }
    }

    private RefreshPaidSubscriptionRequest createRequest(String sourceBotName) {
        return new RefreshPaidSubscriptionRequest(sourceBotName);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpHeaders;
    }

    private String buildUrl(String server, long userId) {
        return UriComponentsBuilder.fromHttpUrl(server)
                .path("/subscription/paid/{userId}/refresh")
                .buildAndExpand(userId).toUriString();
    }
}
