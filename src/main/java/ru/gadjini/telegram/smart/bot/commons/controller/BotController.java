package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/bot")
public class BotController {

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN)
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok("I'm alive");
    }
}
