package ru.gadjini.telegram.smart.bot.commons.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SmartFileUtils {

    private SmartFileUtils() {

    }

    public static boolean isExpired(File file, int days) {
        try {
            FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");

            LocalDateTime creationDateTime = creationTime.toInstant().atZone(TimeUtils.UTC).toLocalDateTime();

            long between = ChronoUnit.DAYS.between(creationDateTime, LocalDateTime.now());

            if (between > days) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static void mkdirs(File file) {
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("Mkdirs " + file.getAbsolutePath() + " failed");
        }
    }

    public static long getLength(String filePath) {
        return new File(filePath).length();
    }
}
