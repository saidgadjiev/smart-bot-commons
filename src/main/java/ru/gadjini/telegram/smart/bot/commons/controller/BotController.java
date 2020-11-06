package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bot")
public class BotController {

    @GetMapping("/health")
    public Mono<ServerResponse> hello() {
        return ServerResponse.ok().build();
    }
}
