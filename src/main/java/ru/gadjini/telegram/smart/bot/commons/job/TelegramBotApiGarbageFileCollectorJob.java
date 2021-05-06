package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.cleaner.GarbageAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TelegramBotApiGarbageFileCollectorJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiGarbageFileCollectorJob.class);

    private static final Set<String> GARBAGE_DIRS = Set.of("animations", "documents", "music", "photos", "profile_photo",
            "stickers", "thumbnails", "video_notes", "videos", "voice");

    private Set<GarbageAlgorithm> algorithms;

    private String dirToClean;

    @Autowired
    public TelegramBotApiGarbageFileCollectorJob(Set<GarbageAlgorithm> algorithms,
                                                 BotApiProperties botApiProperties, BotProperties botProperties) {
        this.algorithms = algorithms;
        this.dirToClean = botApiProperties.getLocalWorkDir() + File.separator + botProperties.getToken();
        LOGGER.debug("Telegram bot api garbage dir({})", dirToClean);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        LOGGER.debug("Start({})", LocalDateTime.now());
        int clean = clean(dirToClean);
        LOGGER.debug("Finish({}, {})", clean, LocalDateTime.now());
    }

    private int clean(String dir) {
        try {
            AtomicInteger counter = new AtomicInteger();
            Files.list(Path.of(dir))
                    .filter(file -> Files.isDirectory(file) && GARBAGE_DIRS.contains(file.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            Files.list(path)
                                    .filter(f -> !Files.isDirectory(f))
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(f -> {
                                        if (deleteIfGarbage(f)) {
                                            counter.incrementAndGet();
                                        }
                                    });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            return counter.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean deleteIfGarbage(File file) {
        GarbageAlgorithm algorithm = getAlgorithm(file);

        if (algorithm != null) {
            if (algorithm.isGarbage(file)) {
                boolean b = FileUtils.deleteQuietly(file);
                if (!b) {
                    LOGGER.debug("Garbage file not deleted({}, {})", file.getAbsolutePath(), algorithm.getClass().getSimpleName());
                } else {
                    LOGGER.debug("Garbage file deleted({})", file.getAbsolutePath());
                }

                return b;
            }
        } else {
            LOGGER.debug("Algorithm not found({})", file.getAbsolutePath());
        }

        return false;
    }

    private GarbageAlgorithm getAlgorithm(File file) {
        for (GarbageAlgorithm algorithm : algorithms) {
            boolean candidate = algorithm.accept(file);

            if (candidate) {
                return algorithm;
            }
        }

        return null;
    }
}
