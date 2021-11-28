package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
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

@Service
@Qualifier("asyncMessage")
public class AsyncMessageService implements MessageService {

    private MessagesQueue messagesQueue;

    private MessageService messageService;

    private MessagesSenderJobProperties messagesSenderJobProperties;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AsyncMessageService(MessagesQueue messagesQueue,
                               @Qualifier("message") MessageService messageService,
                               MessagesSenderJobProperties messagesSenderJobProperties,
                               ApplicationEventPublisher applicationEventPublisher) {
        this.messagesQueue = messagesQueue;
        this.messageService = messageService;
        this.messagesSenderJobProperties = messagesSenderJobProperties;
        this.applicationEventPublisher = applicationEventPublisher;
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
    public Message sendMessage(SendMessage sendMessage) {
        if (messagesSenderJobProperties.isDisable()) {
            return messageService.sendMessage(sendMessage);
        } else {
            messagesQueue.add(sendMessage);
            return null;
        }
    }

    @Override
    public Message sendMessage(SendMessage sendMessage, Object event) {
        if (messagesSenderJobProperties.isDisable()) {
            messagesQueue.add(sendMessage, event);
            return null;
        } else {
            Message message = messageService.sendMessage(sendMessage);
            applicationEventPublisher.publishEvent(new MessageEvent(event, message));

            return message;
        }
    }

    @Override
    public void removeInlineKeyboard(long chatId, Integer messageId) {
        messageService.removeInlineKeyboard(chatId, messageId);
    }

    @Override
    public void editMessage(EditMessageText editMessageText) {
        if (messagesSenderJobProperties.isDisable()) {
            messageService.editMessage(editMessageText);
        } else {
            messagesQueue.add(editMessageText);
        }
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup) {
        if (messagesSenderJobProperties.isDisable()) {
            messageService.editKeyboard(editMessageReplyMarkup);
        } else {
            messagesQueue.add(editMessageReplyMarkup);
        }
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice) {
        if (messagesSenderJobProperties.isDisable()) {
            messageService.sendInvoice(sendInvoice);
        } else {
            messagesQueue.add(sendInvoice);
        }
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        if (messagesSenderJobProperties.isDisable()) {
            messageService.editMessageCaption(editMessageCaption);
        } else {
            messagesQueue.add(editMessageCaption);
        }
    }

    @Override
    public void deleteMessage(long chatId, Integer messageId) {
        messageService.deleteMessage(chatId, messageId);
    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale) {
        messageService.sendErrorMessage(chatId, locale);
    }

}
