package ru.gadjini.telegram.smart.bot.commons.utils;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;

import java.time.ZonedDateTime;

public class JodaTimeUtils {

    private JodaTimeUtils() {
    }

    public static ZonedDateTime plus(ZonedDateTime dateTime, Period period) {
        return dateTime
                .plusYears(period.getYears())
                .plusMonths(period.getMonths())
                .plusWeeks(period.getWeeks())
                .plusDays(period.getDays());
    }

    public static int toDays(Period period) {
        if (period == null) {
            return 0;
        }
        return period.getYears() * 365 + period.getMonths() * 30 + period.getWeeks() * 7 + period.getDays();
    }

    public static PGInterval toPgInterval(Period period) {
        if (period == null) {
            return null;
        }

        return new PGInterval(
                period.getYears(),
                period.getMonths(),
                period.getDays(),
                period.getHours(),
                period.getMinutes(), period.getSeconds()
        );
    }

    public static PGInterval toPgIntervalDays(Period period) {
        if (period == null) {
            return null;
        }

        return new PGInterval(
                0,
                0,
                toDays(period),
                0,
                0,
                0
        );
    }

    public static Period toPeriod(PGInterval interval) {
        if (interval == null) {
            return null;
        }

        return new Period(
                interval.getYears(),
                interval.getMonths(),
                0,
                interval.getDays(),
                interval.getHours(),
                interval.getMinutes(),
                0,
                0
        );
    }
}
