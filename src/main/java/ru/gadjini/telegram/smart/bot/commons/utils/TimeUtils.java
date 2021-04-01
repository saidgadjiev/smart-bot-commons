package ru.gadjini.telegram.smart.bot.commons.utils;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    public static final ZoneId UTC = ZoneId.of("UTC");

    private TimeUtils() {
    }

    public static int getSecondsToTheEndOfTheCurrentDay(int min) {
        LocalDateTime now = LocalDateTime.now(TimeUtils.UTC);
        LocalDateTime endOfCurrentDay = now.toLocalDate().atTime(LocalTime.MAX);

        return Math.max(SmartMath.toExactInt(ChronoUnit.SECONDS.between(now, endOfCurrentDay)), min);
    }

    public static ZonedDateTime toZonedDateTime(LocalDate localDate) {
        return ZonedDateTime.of(localDate, LocalTime.MIN, TimeUtils.UTC);
    }
}
