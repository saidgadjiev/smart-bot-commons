package ru.gadjini.telegram.smart.bot.commons.service.flood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.TgConstants;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.property.FloodControlProperties;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileManager;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FloodWaitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private int finishedDownloadingCounter = 0;

    private Set<String> currentDownloads = new LinkedHashSet<>();

    private SleepTime sleep = new SleepTime(LocalDateTime.now(), 0);

    private FloodControlProperties floodWaitProperties;

    @Autowired
    public FloodWaitController(FloodControlProperties floodWaitProperties) {
        this.floodWaitProperties = floodWaitProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Flood wait properties({}, {}, {}, {})", floodWaitProperties.getFileDownloadingConcurrencyLevel(),
                floodWaitProperties.getSleepAfterXDownloads(), floodWaitProperties.getMaxSleepTime(), floodWaitProperties.isEnableLogging());
    }

    public synchronized void startDownloading(String fileId) {
        AtomicLong sleepTime = new AtomicLong(10);
        if (!isThereAnyFreeDownloadingChannel() || isSleeping(sleepTime)) {
            if (floodWaitProperties.isEnableLogging()) {
                LOGGER.debug("Flood wait " + fileId);
            }
            throw new FloodControlException(sleepTime.get());
        } else {
            acquireDownloadingChannel(fileId);
        }
    }

    public synchronized void cancelDownloading(String fileId, long fileSize) {
        finishDownloading(fileId, fileSize);
    }

    public synchronized void finishDownloading(String fileId, long fileSize) {
        synchronized (this) {
            if (currentDownloads.contains(fileId)) {
                try {
                    ++finishedDownloadingCounter;
                    if (finishedDownloadingCounter % floodWaitProperties.getSleepAfterXDownloads() == 0) {
                        finishedDownloadingCounter = 0;
                        sleep = new SleepTime(LocalDateTime.now(), getSleepTime(fileSize));
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

    private synchronized boolean isSleeping(AtomicLong sleepTime) {
        long seconds = Duration.between(sleep.startedAt, LocalDateTime.now()).toSeconds();
        long secondsLeft = sleep.sleepTime - seconds;

        if (secondsLeft > 0) {
            sleepTime.set(secondsLeft);
        }

        return secondsLeft > 0;
    }

    private long getSleepTime(long fileSize) {
        long sleepOnEverySize = TgConstants.LARGE_FILE_SIZE / floodWaitProperties.getMaxSleepTime();

        return fileSize / sleepOnEverySize;
    }

    private static class SleepTime {

        private final LocalDateTime startedAt;

        private final long sleepTime;

        private SleepTime(LocalDateTime startedAt, long sleepTime) {
            this.startedAt = startedAt;
            this.sleepTime = sleepTime;
        }
    }
}
