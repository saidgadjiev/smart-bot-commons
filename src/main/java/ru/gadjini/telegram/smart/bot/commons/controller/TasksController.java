package ru.gadjini.telegram.smart.bot.commons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;
import ru.gadjini.telegram.smart.bot.commons.model.CancelTaskRequest;
import ru.gadjini.telegram.smart.bot.commons.service.TokenValidator;

@RestController
@RequestMapping("/user/{userId}/tasks")
public class TasksController {

    private TokenValidator tokenValidator;

    private WorkQueueJob workQueueJob;

    @Autowired
    public TasksController(TokenValidator tokenValidator, WorkQueueJob workQueueJob) {
        this.tokenValidator = tokenValidator;
        this.workQueueJob = workQueueJob;
    }

    @PostMapping(value = "/{taskId}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancel(@PathVariable("userId") long userId, @PathVariable("taskId") int taskId,
                                    @RequestHeader("Authorization") String token,
                                    @RequestBody CancelTaskRequest cancelTaskRequest) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        workQueueJob.cancel(userId, cancelTaskRequest.getMessageId(),
                cancelTaskRequest.getCallbackQueryId(), taskId);

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancelUserTasks(@PathVariable("userId") long userId, @RequestHeader("Authorization") String token) {
        if (tokenValidator.isInvalid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        workQueueJob.cancelCurrentTasks(userId);

        return ResponseEntity.ok().build();
    }
}
