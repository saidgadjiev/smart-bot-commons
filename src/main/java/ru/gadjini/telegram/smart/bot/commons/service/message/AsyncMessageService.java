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
import ru.gadjini.telegram.smart.bot.commons.job.TgMethodExecutor;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@Qualifier("asyncMessage")
public class AsyncMessageService implements MessageService {

    private TgMethodExecutor messageSenderJob;

    private MessageService messageService;

    @Autowired
    public AsyncMessageService(TgMethodExecutor messageSenderJob,
                               @Qualifier("message") MessageService messageService) {
        this.messageSenderJob = messageSenderJob;
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
    public boolean isChatMember(String chatId, int userId) {
        return messageService.isChatMember(chatId, userId);
    }

    @Override
    public void sendMessage(SendMessage sendMessage) {
        sendMessage(sendMessage, null);
    }

    @Override
    public void sendMessage(SendMessage sendMessage, Consumer<Message> callback) {
        messageSenderJob.push(() -> messageService.sendMessage(sendMessage, callback));
    }

    @Override
    public void removeInlineKeyboard(long chatId, int messageId) {
        messageSenderJob.push(() -> messageService.removeInlineKeyboard(chatId, messageId));
    }

    @Override
    public void editMessage(EditMessageText editMessageText, boolean ignoreException) {
        messageSenderJob.push(() -> messageService.editMessage(editMessageText, ignoreException));
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException) {
        messageSenderJob.push(() -> messageService.editKeyboard(editMessageReplyMarkup, ignoreException));
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice, Consumer<Message> callback) {
        messageSenderJob.push(() -> messageService.sendInvoice(sendInvoice, callback));
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        messageSenderJob.push(() -> messageService.editMessageCaption(editMessageCaption));
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
        messageSenderJob.push(() -> messageService.deleteMessage(chatId, messageId));
    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale) {
        messageService.sendErrorMessage(chatId, locale);
    }

}
