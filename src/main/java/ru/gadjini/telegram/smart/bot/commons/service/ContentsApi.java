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
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.model.DeleteContentRequest;
import ru.gadjini.telegram.smart.bot.commons.property.AuthProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;

@Service
public class ContentsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentsApi.class);

    private ServerProperties serverProperties;

    private AuthProperties authProperties;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ContentsApi(ServerProperties serverProperties, AuthProperties authProperties) {
        this.serverProperties = serverProperties;
        this.authProperties = authProperties;
    }

    public void delete(SmartTempFile tempFile) {
        DeleteContentRequest deleteContentRequest = new DeleteContentRequest(tempFile.getAbsolutePath(), tempFile.isDeleteParentDir());
        HttpEntity<DeleteContentRequest> entity = new HttpEntity<>(deleteContentRequest);

        ResponseEntity<Void> response = restTemplate.postForEntity(buildDeleteContentUrl(), addAuthHeader(entity), Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("Error delete content({})", tempFile.getAbsolutePath());
        }
    }

    private HttpEntity<?> addAuthHeader(HttpEntity<?> httpEntity) {
        httpEntity.getHeaders().add(HttpHeaders.AUTHORIZATION, authProperties.getToken());

        return httpEntity;
    }

    private String buildDeleteContentUrl() {
        return UriComponentsBuilder.fromHttpUrl(serverProperties.getPrimaryServer())
                .build()
                .toUriString();
    }
}
