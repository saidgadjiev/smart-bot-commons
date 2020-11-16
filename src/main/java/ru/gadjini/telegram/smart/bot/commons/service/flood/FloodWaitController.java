package ru.gadjini.telegram.smart.bot.commons.service.flood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.property.FloodControlProperties;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FloodWaitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private int finishedDownloadingCounter = 0;

    private Set<String> currentDownloads = new LinkedHashSet<>();

    private LocalDateTime sleepStartedAt;

    private FloodControlProperties floodWaitProperties;

    @Autowired
    public FloodWaitController(FloodControlProperties floodWaitProperties) {
        this.floodWaitProperties = floodWaitProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("File wait properties({}, {}, {}, {})", floodWaitProperties.getFileDownloadingConcurrencyLevel(),
                floodWaitProperties.getSleepAfterXDownloads(), floodWaitProperties.getSleepTime(), floodWaitProperties.isEnableLogging());
    }

    public synchronized void startDownloading(String fileId) {
        if (!isThereAnyFreeDownloadingChannel() || isSleeping()) {
            if (floodWaitProperties.isEnableLogging()) {
                LOGGER.debug("Flood wait " + fileId);
            }
            throw new FloodWaitException();
        } else {
            acquireDownloadingChannel(fileId);
        }
    }

    public synchronized void cancelDownloading(String fileId) {
        finishDownloading(fileId);
    }

    public synchronized void finishDownloading(String fileId) {
        synchronized (this) {
            if (currentDownloads.contains(fileId)) {
                try {
                    ++finishedDownloadingCounter;
                    if (finishedDownloadingCounter % floodWaitProperties.getSleepAfterXDownloads() == 0) {
                        finishedDownloadingCounter = 0;
                        sleepStartedAt = LocalDateTime.now();
                    }
                } finally {
                    releaseDownloadingChannel(fileId);
                }
            }
        }
    }

    private synchronized void releaseDownloadingChannel(String fileId) {
        currentDownloads.remove(fileId);
    }

    private synchronized void acquireDownloadingChannel(String fileId) {
        if (currentDownloads.size() < floodWaitProperties.getFileDownloadingConcurrencyLevel()) {
            currentDownloads.add(fileId);
        }
    }

    private synchronized boolean isThereAnyFreeDownloadingChannel() {
        return currentDownloads.size() < floodWaitProperties.getFileDownloadingConcurrencyLevel();
    }

    private synchronized boolean isSleeping() {
        if (sleepStartedAt == null) {
            return false;
        }
        long seconds = Duration.between(sleepStartedAt, LocalDateTime.now()).toSeconds();

        return seconds <= floodWaitProperties.getSleepTime();
    }
}
