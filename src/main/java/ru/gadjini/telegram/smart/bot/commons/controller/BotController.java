package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gadjini.telegram.smart.bot.commons.model.BotHealth;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/bot")
public class BotController {

    @Value("${git.commit.id}")
    private String commitId;

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(new BotHealth("I'm alive", commitId));
    }
}
