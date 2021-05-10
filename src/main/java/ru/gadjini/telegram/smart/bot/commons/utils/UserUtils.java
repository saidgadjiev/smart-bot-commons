package ru.gadjini.telegram.smart.bot.commons.utils;

public class UserUtils {

    private UserUtils() {
    }

    public static String userLink(int id) {
        StringBuilder link = new StringBuilder();

        link.append("<a href=\"tg://user?id=").append(id).append("\">").append(id).append("</a>");

        return link.toString();
    }
}
