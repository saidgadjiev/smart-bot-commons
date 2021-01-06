package ru.gadjini.telegram.smart.bot.commons.utils;

import java.io.File;

public class SmartFileUtils {

    private SmartFileUtils() {

    }

    public static long getLength(String filePath) {
        return new File(filePath).length();
    }
}
