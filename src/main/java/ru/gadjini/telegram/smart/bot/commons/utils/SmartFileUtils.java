package ru.gadjini.telegram.smart.bot.commons.utils;

import java.io.File;

public class SmartFileUtils {

    private SmartFileUtils() {

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
