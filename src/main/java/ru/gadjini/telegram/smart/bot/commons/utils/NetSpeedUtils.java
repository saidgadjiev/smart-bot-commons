package ru.gadjini.telegram.smart.bot.commons.utils;

import java.util.Map;

public class NetSpeedUtils {

    private NetSpeedUtils() {

    }

    public static String toSpeed(long size) {
        long power = 1024;
        int raisedToPow = 0;
        Map<Integer, String> dict_power_n = Map.of(0, "", 1, "K", 2, "M", 3, "G", 4, "T");
        while(size > power) {
            size /= power;
            raisedToPow += 1;
        }

        return size + " " + dict_power_n.get(raisedToPow) + "B/s";
    }
}
