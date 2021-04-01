package ru.gadjini.telegram.smart.bot.commons.utils;

import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String getText(Message message) {
        if (message.hasText()) {
            return message.getText().trim();
        }

        return "";
    }
}
