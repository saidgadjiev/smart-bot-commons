package ru.gadjini.telegram.smart.bot.commons.utils;

public class TextUtils {

    private TextUtils() {

    }

    public static String removeHtmlTags(String str) {
        return str.replaceAll("<.*?>", "");
    }
}
