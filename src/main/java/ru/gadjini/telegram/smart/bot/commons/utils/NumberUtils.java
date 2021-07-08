package ru.gadjini.telegram.smart.bot.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class NumberUtils {

    private NumberUtils() {

    }

    public static boolean isEvent(Integer val) {
        return val != null && val % 2 == 0;
    }

    public static String toString(double number, int decimalPoints) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("#", decimalPoints));

        return decimalFormat.format(number);
    }

    public static double round(double number, int decimalPoints) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("#", decimalPoints));
        String strNumber = decimalFormat.format(number);

        return Double.parseDouble(strNumber);
    }
}
