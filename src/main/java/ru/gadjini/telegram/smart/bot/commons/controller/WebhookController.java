package ru.gadjini.telegram.smart.bot.commons.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.bot.SmartWebhookBot;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;

import javax.ws.rs.core.MediaType;

@RestController
@Profile({SmartBotConfiguration.PROFILE_PROD, SmartBotConfiguration.PROFILE_LOAD_TEST})
@RequestMapping("/callback")
public class WebhookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookController.class);

    private SmartWebhookBot smartBot;

    @Autowired
    public WebhookController(SmartWebhookBot smartBot) {
        this.smartBot = smartBot;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> updateReceived(@RequestBody Update update) {
        try {
            smartBot.onWebhookUpdateReceived(update);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
