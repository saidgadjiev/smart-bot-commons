package ru.gadjini.telegram.smart.bot.commons.service.telegram;

public class TgUrlBuilder {

    private static final String T_ME = "https://t.me/";

    public String tMe(String botName) {
        return T_ME + botName;
    }
}
