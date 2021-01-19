package ru.gadjini.telegram.smart.bot.commons.service.keyboard.smart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartButtonFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SmartUploadKeyboardService {

    private SmartButtonFactory smartButtonFactory;

    @Autowired
    public SmartUploadKeyboardService(SmartButtonFactory smartButtonFactory) {
        this.smartButtonFactory = smartButtonFactory;
    }

    public InlineKeyboardMarkup getSmartUploadKeyboard(int uploadId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.supportsStreaming(uploadId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(smartButtonFactory.doSmartUpload(uploadId, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }
}
