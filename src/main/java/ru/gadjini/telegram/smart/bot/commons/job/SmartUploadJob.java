package ru.gadjini.telegram.smart.bot.commons.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.service.queue.UploadQueueService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
public class SmartUploadJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartUploadJob.class);

    private static final int EXPIRATION_TIME_IN_SECONDS = 30 * 60;

    private UploadQueueService uploadQueueService;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Value("${enable.jobs.logging:false}")
    private boolean enableJobsLogging;

    @Autowired
    public SmartUploadJob(UploadQueueService uploadQueueService) {
        this.uploadQueueService = uploadQueueService;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", disableJobs);
        LOGGER.debug("Enable jobs logging {}", enableJobsLogging);
    }

    @Scheduled(fixedDelay = 1000)
    public void autoSend() {
        if (disableJobs) {
            return;
        }
        if (enableJobsLogging) {
            LOGGER.debug("Start({})", LocalDateTime.now());
        }
        uploadQueueService.setWaitingExpiredSmartUploads(EXPIRATION_TIME_IN_SECONDS);
        if (enableJobsLogging) {
            LOGGER.debug("Start({})", LocalDateTime.now());
        }
    }
}
