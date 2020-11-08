package ru.gadjini.telegram.smart.bot.commons.dao.command.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface ReplyKeyboardDao {
    void store(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup);

    ReplyKeyboardMarkup get(long chatId);
}
