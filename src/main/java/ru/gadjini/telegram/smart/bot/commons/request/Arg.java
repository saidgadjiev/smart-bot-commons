package ru.gadjini.telegram.smart.bot.commons.request;

public enum Arg {

    QUEUE_ITEM_ID("a");

    private final String key;

    Arg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
