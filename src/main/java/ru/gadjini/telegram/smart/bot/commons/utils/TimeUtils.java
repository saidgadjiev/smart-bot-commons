package ru.gadjini.telegram.smart.bot.commons.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    private TimeUtils() {
    }

    public static int getSecondsToTheEndOfTheCurrentDay() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endOfCurrentDay = now.toLocalDate().atTime(LocalTime.MAX);

        return SmartMath.toExactInt(ChronoUnit.SECONDS.between(now, endOfCurrentDay));
    }
}
