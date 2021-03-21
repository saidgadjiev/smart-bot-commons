package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SmartInlineKeyboardService {

    private SmartButtonFactory buttonFactory;

    @Autowired
    public SmartInlineKeyboardService(SmartButtonFactory buttonFactory) {
        this.buttonFactory = buttonFactory;
    }

    public InlineKeyboardMarkup getProcessingKeyboard(int queueItemId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.cancelQueryItem(queueItemId, locale)));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getWaitingKeyboard(int queueItemId, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.updateQueryStatus(queueItemId, locale)));
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.cancelQueryItem(queueItemId, locale)));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getPaymentKeyboard(String paymentBotName, Locale locale) {
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardMarkup();
        inlineKeyboardMarkup.getKeyboard().add(List.of(buttonFactory.goToPaymentBot(paymentBotName, locale)));

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());

        return inlineKeyboardMarkup;
    }
}
