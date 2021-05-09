package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

@RestController
@RequestMapping("/subscription/paid")
public class PaidSubscriptionController {

    private TokenValidator tokenValidator;

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    @Autowired
    public PaidSubscriptionController(TokenValidator tokenValidator,
                                      PaidSubscriptionRemoveService paidSubscriptionRemoveService) {
        this.tokenValidator = tokenValidator;
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
    }

    @PostMapping("/{userId}/refresh")
    public ResponseEntity<?> refresh(@PathVariable("userId") int userId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        paidSubscriptionRemoveService.refreshPaidSubscription(userId);

        return ResponseEntity.ok().build();
    }
}
