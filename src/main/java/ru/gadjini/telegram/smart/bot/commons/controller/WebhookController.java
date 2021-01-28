package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile(SmartBotConfiguration.PROFILE_PROD)
@RequestMapping("/callback")
public class WebhookController {

    private SmartWebhookBot smartBot;

    @Autowired
    public WebhookController(SmartWebhookBot smartBot) {
        this.smartBot = smartBot;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> updateReceived(@RequestBody Update update) {
        smartBot.onWebhookUpdateReceived(update);

        return ResponseEntity.ok().build();
    }
}
