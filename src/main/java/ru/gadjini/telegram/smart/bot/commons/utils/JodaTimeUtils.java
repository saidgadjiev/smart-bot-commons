package ru.gadjini.telegram.smart.bot.commons.utils;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;

public class JodaTimeUtils {

    private JodaTimeUtils() {
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
