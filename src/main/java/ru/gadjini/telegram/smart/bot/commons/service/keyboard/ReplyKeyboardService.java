package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.ReplyKeyboard;

public interface ReplyKeyboardService {

    ReplyKeyboard getMainMenu(long chatId);
}
