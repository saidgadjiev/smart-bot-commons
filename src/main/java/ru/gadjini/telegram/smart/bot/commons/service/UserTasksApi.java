package ru.gadjini.telegram.smart.bot.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

@Service
public class UserTasksApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTasksApi.class);

    private RestTemplate restTemplate = new RestTemplate();

    private ServerProperties serverProperties;

    private AuthProperties authProperties;

    @Autowired
    public UserTasksApi(ServerProperties serverProperties, AuthProperties authProperties) {
        this.serverProperties = serverProperties;
        this.authProperties = authProperties;
    }

    public boolean cancel(int serverNumber, long userId, int taskId) {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(buildCancelTaskUrl(
                serverProperties.getServer(serverNumber), userId, taskId), addAuthHeader(new HttpEntity<>(null)), Void.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("Cancel task failed({}, {}, {}, {})", responseEntity.getStatusCodeValue(), userId, serverNumber, taskId);

            return false;
        }

        return true;
    }

    private HttpEntity<?> addAuthHeader(HttpEntity<?> httpEntity) {
        httpEntity.getHeaders().add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpEntity;
    }

    private String buildCancelTaskUrl(String server, long userId, int taskId) {
        return UriComponentsBuilder.fromHttpUrl(server)
                .path("/user/{userId}/tasks/{taskId}/cancel")
                .buildAndExpand(userId, taskId).toUriString();
    }
}
