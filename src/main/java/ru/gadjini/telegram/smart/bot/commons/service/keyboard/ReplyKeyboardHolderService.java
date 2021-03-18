package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface ReplyKeyboardHolderService extends ReplyKeyboardService {

    ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId);
}
