package ru.gadjini.telegram.smart.bot.commons.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gadjini.telegram.smart.bot.commons.model.BotHealth;
import ru.gadjini.telegram.smart.bot.commons.property.ProfileProperties;
import ru.gadjini.telegram.smart.bot.commons.service.process.FFmpegProcessExecutor;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping("/bot")
public class BotController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotController.class);

    @Value("${git.commit.id}")
    private String commitId;

    private ProfileProperties profileProperties;

    private FFmpegProcessExecutor processExecutor;

    @Autowired
    public BotController(ProfileProperties profileProperties, FFmpegProcessExecutor processExecutor) {
        this.profileProperties = profileProperties;
        this.processExecutor = processExecutor;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Commit({})", commitId);
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(new BotHealth("I'm alive", commitId, profileProperties.getActive(), processExecutor.scannersCount()));
    }
}
