package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.service.message.queue.MessagesQueue;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@Qualifier("asyncMessage")
public class AsyncMessageService implements MessageService {

    private MessagesQueue messagesQueue;

    private MessageService messageService;

    @Autowired
    public AsyncMessageService(MessagesQueue messagesQueue,
                               @Qualifier("message") MessageService messageService) {
        this.messagesQueue = messagesQueue;
        this.messageService = messageService;
    }

    @Override
    public void sendAnswerPreCheckoutQuery(AnswerPreCheckoutQuery answerPreCheckoutQuery) {
        messageService.sendAnswerPreCheckoutQuery(answerPreCheckoutQuery);
    }

    @Override
    public void sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        messageService.sendAnswerCallbackQuery(answerCallbackQuery);
    }

    @Override
    public boolean isChatMember(String chatId, long userId) {
        return messageService.isChatMember(chatId, userId);
    }

    @Override
    public void sendMessage(SendMessage sendMessage) {
        messagesQueue.add(sendMessage);
    }

    @Override
    public void sendMessage(SendMessage sendMessage, Consumer<Message> callback) {
        messageService.sendMessage(sendMessage, callback);
    }

    @Override
    public void removeInlineKeyboard(long chatId, int messageId) {
        messageService.removeInlineKeyboard(chatId, messageId);
    }

    @Override
    public void editMessage(EditMessageText editMessageText, boolean ignoreException) {
        messageService.editMessage(editMessageText, ignoreException);
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException) {
        messageService.editKeyboard(editMessageReplyMarkup, ignoreException);
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice, Consumer<Message> callback) {
        messageService.sendInvoice(sendInvoice, callback);
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        messageService.editMessageCaption(editMessageCaption);
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
        messageService.deleteMessage(chatId, messageId);
    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale) {
        messageService.sendErrorMessage(chatId, locale);
    }

}
