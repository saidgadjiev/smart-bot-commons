package ru.gadjini.telegram.smart.bot.commons.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gadjini.telegram.smart.bot.commons.event.RefreshPaidSubscriptionEvent;
import ru.gadjini.telegram.smart.bot.commons.model.RefreshPaidSubscriptionRequest;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;

@RestController
@RequestMapping("/subscription/paid")
public class PaidSubscriptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaidSubscriptionController.class);

    private TokenValidator tokenValidator;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public PaidSubscriptionController(TokenValidator tokenValidator,
                                      ApplicationEventPublisher applicationEventPublisher) {
        this.tokenValidator = tokenValidator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/{userId}/refresh")
    public ResponseEntity<?> refresh(@PathVariable("userId") long userId,
                                     @RequestBody RefreshPaidSubscriptionRequest request,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        LOGGER.debug("Refresh subscription({})", userId);

        try {
            if (tokenValidator.isInvalid(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            applicationEventPublisher.publishEvent(new RefreshPaidSubscriptionEvent(userId, request.getSourceBotName()));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
