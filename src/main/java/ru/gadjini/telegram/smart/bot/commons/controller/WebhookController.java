package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.bot.SmartBot;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/callback")
public class WebhookController {

    private SmartBot smartBot;

    @Autowired
    public WebhookController(SmartBot smartBot) {
        this.smartBot = smartBot;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> updateReceived(@RequestBody Update update) {
        smartBot.onWebhookUpdateReceived(update);

        return ResponseEntity.ok().build();
    }
}
