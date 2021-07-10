package ru.gadjini.telegram.smart.bot.commons.utils;

public class TelegramLinkUtils {

    private TelegramLinkUtils() {
    }

    public static String botLink(String botName, String startParameter) {
        return "<a href=\"https://t.me/" + botName + "?start=" + startParameter + "\">@" + botName + "</a>";
    }

    public static String userLink(long id) {
        return "<a href=\"tg://user?id=" + id + "\">" + id + "</a>";
    }

    public static String mention(String name) {
        return "@" + name;
    }
}
