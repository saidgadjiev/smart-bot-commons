package ru.gadjini.telegram.smart.bot.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class NumberUtils {

    private NumberUtils() {

    }

    public static String toString(double number) {
        if (number % 1 == 0) {
            return String.valueOf((int) number);
        }

        return String.valueOf(number);
    }

    public static String toString(double number, int decimalPoints) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("0", decimalPoints));

        return decimalFormat.format(number);
    }

    public static double round(double number, int decimalPoints) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("0", decimalPoints));
        String strNumber = decimalFormat.format(number);

        return Double.parseDouble(strNumber);
    }
}
