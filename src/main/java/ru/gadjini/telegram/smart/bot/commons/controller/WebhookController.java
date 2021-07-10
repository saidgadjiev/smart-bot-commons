package ru.gadjini.telegram.smart.bot.commons.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.Constants;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.bot.SmartWebhookBot;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;

import javax.ws.rs.core.MediaType;

@RestController
@Profile({Profiles.PROFILE_PROD_PRIMARY})
@RequestMapping("/" + Constants.WEBHOOK_URL_PATH)
public class WebhookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookController.class);

    private SmartWebhookBot smartBot;

    @Autowired
    public WebhookController(SmartWebhookBot smartBot) {
        this.smartBot = smartBot;
    }

    @PostMapping(value = "/{botPath}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> updateReceived(@PathVariable("botPath") String botPath, @RequestBody Update update) {
        try {
            smartBot.onWebhookUpdateReceived(update);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
