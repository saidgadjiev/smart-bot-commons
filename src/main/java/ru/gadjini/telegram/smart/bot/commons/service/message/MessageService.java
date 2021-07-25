package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.utils.TextUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public interface MessageService {

    void sendAnswerPreCheckoutQuery(AnswerPreCheckoutQuery answerPreCheckoutQuery);

    void sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery);

    boolean isChatMember(String chatId, long userId);

    void sendMessage(SendMessage sendMessage);

    void sendMessage(SendMessage sendMessage, Consumer<Message> callback);

    void removeInlineKeyboard(long chatId, int messageId);

    void editMessage(EditMessageText messageContext, boolean ignoreException);

    default void editMessage(String text, InlineKeyboardMarkup inlineKeyboardMarkup,
                             EditMessageText messageContext, boolean ignoreException) {
        if (Objects.equals(text, TextUtils.removeHtmlTags(messageContext.getText()))
                && Objects.equals(inlineKeyboardMarkup, messageContext.getReplyMarkup())) {
            return;
        }
        editMessage(messageContext, ignoreException);
    }

    default void editMessage(EditMessageText messageContext) {
        editMessage(messageContext, true);
    }

    default void editMessage(String text, InlineKeyboardMarkup inlineKeyboardMarkup, EditMessageText messageContext) {
        if (Objects.equals(text, TextUtils.removeHtmlTags(messageContext.getText()))
                && Objects.equals(inlineKeyboardMarkup, messageContext.getReplyMarkup())) {
            return;
        }
        editMessage(messageContext, true);
    }

    void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException);

    default void editKeyboard(InlineKeyboardMarkup currentKeyboard, EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException) {
        if (Objects.equals(currentKeyboard, editMessageReplyMarkup.getReplyMarkup())) {
            return;
        }
        editKeyboard(editMessageReplyMarkup, ignoreException);
    }

    void editMessageCaption(EditMessageCaption context);

    void deleteMessage(long chatId, int messageId);

    void sendErrorMessage(long chatId, Locale locale);

    void sendInvoice(SendInvoice sendInvoice, Consumer<Message> callback);

    default void sendInvoice(SendInvoice sendInvoice) {
        sendInvoice(sendInvoice, null);
    }
}
