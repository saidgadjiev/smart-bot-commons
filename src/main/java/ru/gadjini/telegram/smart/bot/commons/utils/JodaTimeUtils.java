package ru.gadjini.telegram.smart.bot.commons.utils;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;

import java.time.LocalDate;

public class JodaTimeUtils {

    private JodaTimeUtils() {
    }

    public static LocalDate plus(LocalDate localDate, Period period) {
        return localDate
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
                period.getDays() + period.getWeeks() * 7,
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
        int days = interval.getDays();

        return new Period(
                interval.getYears(),
                interval.getMonths(),
                days / 7, days % 7,
                interval.getHours(),
                interval.getMinutes(),
                0,
                0
        );
    }
}
