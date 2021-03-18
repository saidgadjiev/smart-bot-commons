package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Locale;

public class DummyReplyKeyboardHolderService implements ReplyKeyboardHolderService {

    @Override
    public ReplyKeyboard mainMenuKeyboard(long chatId, Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReplyKeyboardMarkup smartFileFeatureKeyboard(long chatId, Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReplyKeyboardMarkup languageKeyboard(long chatId, Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId) {
        throw new UnsupportedOperationException();
    }
}
