package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

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

@Service
public class SmartReplyKeyboardService {

    private LocalisationService localisationService;

    @Autowired
    public SmartReplyKeyboardService(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public ReplyKeyboardMarkup languageKeyboard(Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardService.replyKeyboardMarkup();

        List<String> languages = new ArrayList<>();
        for (Locale l : localisationService.getSupportedLocales()) {
            languages.add(StringUtils.capitalize(l.getDisplayLanguage(l)));
        }
        replyKeyboardMarkup.getKeyboard().add(ReplyKeyboardService.keyboardRow(languages.toArray(new String[0])));
        replyKeyboardMarkup.getKeyboard().add(ReplyKeyboardService.keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup smartFileFeatureKeyboard(Locale locale) {
        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardService.replyKeyboardMarkup();
        replyKeyboardMarkup.getKeyboard().add(
                ReplyKeyboardService.keyboardRow(localisationService.getMessage(MessagesProperties.ENABLE_COMMAND_NAME, locale),
                        localisationService.getMessage(MessagesProperties.DISABLE_COMMAND_NAME, locale)));
        replyKeyboardMarkup.getKeyboard().add(ReplyKeyboardService.keyboardRow(localisationService.getMessage(MessagesProperties.GO_BACK_COMMAND_NAME, locale)));

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardRemove removeKeyboard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        return replyKeyboardRemove;
    }
}
