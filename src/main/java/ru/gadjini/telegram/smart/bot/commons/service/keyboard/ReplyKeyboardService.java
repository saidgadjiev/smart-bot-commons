package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.ReplyKeyboard;

import java.util.Locale;

public interface ReplyKeyboardService {

    ReplyKeyboard getMainMenu(long chatId, Locale locale);

    ReplyKeyboard languageKeyboard(long chatId, Locale locale);
}
