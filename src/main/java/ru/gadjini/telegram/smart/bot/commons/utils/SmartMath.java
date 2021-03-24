package ru.gadjini.telegram.smart.bot.commons.utils;

public class SmartMath {

    private SmartMath() {

    }

    public static int toExactInt(long value) {
        if ((int) value != value) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }
}
