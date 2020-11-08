package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Locale;
import java.util.function.Consumer;

public interface MessageService {

    void sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery);

    boolean isChatMember(String chatId, int userId);

    void sendMessage(SendMessage sendMessage);

    void sendMessage(SendMessage sendMessage, Consumer<Message> callback);

    void removeInlineKeyboard(long chatId, int messageId);

    void editMessage(EditMessageText messageContext, boolean ignoreException);

    void editMessageCaption(EditMessageCaption context);

    void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale);

    void deleteMessage(long chatId, int messageId);

    void sendErrorMessage(long chatId, Locale locale);

}
