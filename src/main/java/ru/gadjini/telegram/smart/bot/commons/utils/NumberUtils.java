package ru.gadjini.telegram.smart.bot.commons.utils;

public class NumberUtils {

    private NumberUtils() {

    }

    public static String toString(double price) {
        if (price % 1 == 0) {
            return String.valueOf((int) price);
        }

        return String.valueOf(price);
    }
}
