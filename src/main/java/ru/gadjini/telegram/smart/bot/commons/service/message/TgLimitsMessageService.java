package ru.gadjini.telegram.smart.bot.commons.service.message;

import com.google.common.base.Splitter;
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
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static ru.gadjini.telegram.smart.bot.commons.common.TgConstants.TEXT_LENGTH_LIMIT;

@Service
@TgMessageLimitsControl
public class TgLimitsMessageService implements MessageService {

    private MessageService messageService;

    @Autowired
    public void setMessageService(@Qualifier("asyncMessage") MessageService messageService) {
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
        messageService.sendMessage(sendMessage);
    }

    @Override
    public void sendMessage(SendMessage sendMessage, Consumer<Message> callback) {
        if (sendMessage.getText().length() < TEXT_LENGTH_LIMIT) {
            messageService.sendMessage(sendMessage, callback);
        } else {
            List<String> parts = new ArrayList<>();
            Splitter.fixedLength(TEXT_LENGTH_LIMIT)
                    .split(sendMessage.getText())
                    .forEach(parts::add);
            for (int i = 0; i < parts.size() - 1; ++i) {
                SendMessage msg = SendMessage.builder()
                        .chatId(String.valueOf(sendMessage.getChatId()))
                        .text(parts.get(i))
                        .replyToMessageId(sendMessage.getReplyToMessageId())
                        .disableWebPagePreview(sendMessage.getDisableWebPagePreview())
                        .parseMode(sendMessage.getParseMode())
                        .build();
                messageService.sendMessage(msg);
            }

            SendMessage msg = SendMessage.builder()
                    .chatId(String.valueOf(sendMessage.getChatId()))
                    .text(parts.get(parts.size() - 1))
                    .replyToMessageId(sendMessage.getReplyToMessageId())
                    .disableWebPagePreview(sendMessage.getDisableWebPagePreview())
                    .parseMode(sendMessage.getParseMode())
                    .replyMarkup(sendMessage.getReplyMarkup()).build();
            messageService.sendMessage(msg, callback);
        }
    }

    @Override
    public void removeInlineKeyboard(long chatId, int messageId) {
        messageService.removeInlineKeyboard(chatId, messageId);
    }

    @Override
    public void editMessage(EditMessageText editMessageText) {
        messageService.editMessage(editMessageText);
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup) {
        messageService.editKeyboard(editMessageReplyMarkup);
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice) {
        messageService.sendInvoice(sendInvoice);
    }

    @Override
    public void editMessageCaption(EditMessageCaption context) {
        messageService.editMessageCaption(context);
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
