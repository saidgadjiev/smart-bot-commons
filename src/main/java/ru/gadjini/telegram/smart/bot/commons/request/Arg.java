package ru.gadjini.telegram.smart.bot.commons.request;

public enum Arg {

    QUEUE_ITEM_ID("a"),
    SUPPORTS_STREAMING("aa");

    private final String key;

    Arg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
