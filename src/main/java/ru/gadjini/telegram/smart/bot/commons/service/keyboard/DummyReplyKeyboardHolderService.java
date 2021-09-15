package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.dao.command.keyboard.ReplyKeyboardDao;

import java.util.Locale;

public class DummyReplyKeyboardHolderService implements ReplyKeyboardHolderService {

    private ReplyKeyboardDao replyKeyboardDao;

    private SmartReplyKeyboardService smartReplyKeyboardService;

    public DummyReplyKeyboardHolderService(ReplyKeyboardDao replyKeyboardDao, SmartReplyKeyboardService smartReplyKeyboardService) {
        this.replyKeyboardDao = replyKeyboardDao;
        this.smartReplyKeyboardService = smartReplyKeyboardService;
    }

    @Override
    public ReplyKeyboard mainMenuKeyboard(long chatId, Locale locale) {
        setCurrentKeyboard(chatId, ReplyKeyboardService.replyKeyboardMarkup());
        return smartReplyKeyboardService.removeKeyboard();
    }

    @Override
    public ReplyKeyboardMarkup goBackKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, smartReplyKeyboardService.goBackKeyboard(chatId, locale));
    }

    @Override
    public ReplyKeyboardMarkup smartFileFeatureKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, smartReplyKeyboardService.smartFileFeatureKeyboard(locale));
    }

    @Override
    public ReplyKeyboardMarkup languageKeyboard(long chatId, Locale locale) {
        return setCurrentKeyboard(chatId, smartReplyKeyboardService.languageKeyboard(locale));
    }

    @Override
    public ReplyKeyboardMarkup getCurrentReplyKeyboard(long chatId) {
        return replyKeyboardDao.get(chatId);
    }

    private ReplyKeyboardMarkup setCurrentKeyboard(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        replyKeyboardDao.store(chatId, replyKeyboardMarkup);

        return replyKeyboardMarkup;
    }
}
