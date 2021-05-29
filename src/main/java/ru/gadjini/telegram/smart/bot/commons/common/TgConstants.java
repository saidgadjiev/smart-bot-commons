package ru.gadjini.telegram.smart.bot.commons.common;

public class TgConstants {

    private TgConstants() {

    }

    //2000 MB
    public static final long LARGE_FILE_SIZE = 2000 * 1024 * 1024;

    public static final long VIDEO_NOTE_MAX_FILE_SIZE = 8 * 1024 * 1024;

    public static final long VIDEO_NOTE_MAX_LENGTH = 60;

    public static final int PAYMENTS_AMOUNT_FACTOR = 100;

    public static final int TEXT_LENGTH_LIMIT = 4000;

    public static final String USD_CURRENCY = "usd";
}
