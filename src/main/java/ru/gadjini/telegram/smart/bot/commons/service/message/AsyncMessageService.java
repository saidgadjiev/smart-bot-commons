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
import ru.gadjini.telegram.smart.bot.commons.property.MessagesSenderJobProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.queue.MessagesQueue;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@Qualifier("asyncMessage")
public class AsyncMessageService implements MessageService {

    private MessagesQueue messagesQueue;

    private MessageService messageService;

    private MessagesSenderJobProperties messagesSenderJobProperties;

    @Autowired
    public AsyncMessageService(MessagesQueue messagesQueue,
                               @Qualifier("message") MessageService messageService,
                               MessagesSenderJobProperties messagesSenderJobProperties) {
        this.messagesQueue = messagesQueue;
        this.messageService = messageService;
        this.messagesSenderJobProperties = messagesSenderJobProperties;
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
        if (messagesSenderJobProperties.isDisableAsync()) {
            messageService.sendMessage(sendMessage);
        } else {
            messagesQueue.add(sendMessage);
        }
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
    public void editMessage(EditMessageText editMessageText) {
        if (messagesSenderJobProperties.isDisableAsync()) {
            messageService.editMessage(editMessageText);
        } else {
            messagesQueue.add(editMessageText);
        }
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup) {
        if (messagesSenderJobProperties.isDisableAsync()) {
            messageService.editKeyboard(editMessageReplyMarkup);
        } else {
            messagesQueue.add(editMessageReplyMarkup);
        }
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice) {
        if (messagesSenderJobProperties.isDisableAsync()) {
            messageService.sendInvoice(sendInvoice);
        } else {
            messagesQueue.add(sendInvoice);
        }
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        if (messagesSenderJobProperties.isDisableAsync()) {
            messageService.editMessageCaption(editMessageCaption);
        } else {
            messagesQueue.add(editMessageCaption);
        }
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
