package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService.keyboardRow;
import static ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService.replyKeyboardMarkup;

@Service
public class SmartReplyKeyboardService {

    private LocalisationService localisationService;

    @Autowired
    public SmartReplyKeyboardService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public ReplyKeyboardMarkup languageKeyboard(Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        List<List<String>> languages = new ArrayList<>();
        List<List<Locale>> supportedLocalesParts = Lists.partition(localisationService.getSupportedLocales(), 2);
        for (List<Locale> supportedLocales : supportedLocalesParts) {
            List<String> langs = new ArrayList<>();
            for (Locale l : supportedLocales) {
                langs.add(StringUtils.capitalize(l.getDisplayLanguage(l)));
            }
            languages.add(langs);
        }
        for (List<String> langs : languages) {
            replyKeyboardMarkup.getKeyboard().add(keyboardRow(langs.toArray(new String[0])));
        }
        replyKeyboardMarkup.getKeyboard().add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup goBackKeyboard(long chatId, Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();

        replyKeyboardMarkup.getKeyboard().add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup smartFileFeatureKeyboard(Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardMarkup();
        replyKeyboardMarkup.getKeyboard().add(
                keyboardRow(localisationService.getMessage(MessagesProperties.ENABLE_COMMAND_NAME, locale),
                        localisationService.getMessage(MessagesProperties.DISABLE_COMMAND_NAME, locale)));
        replyKeyboardMarkup.getKeyboard().add(keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardRemove removeKeyboard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        return replyKeyboardRemove;
    }
}
