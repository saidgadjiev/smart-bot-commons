package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Locale;

@Service
@Qualifier("message")
@SuppressWarnings("PMD")
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    private LocalisationService localisationService;

    private TelegramBotApiService telegramService;

    @Autowired
    public MessageServiceImpl(LocalisationService localisationService,
                              TelegramBotApiService telegramService) {
        this.localisationService = localisationService;
        this.telegramService = telegramService;
    }

    @Override
    public void sendAnswerPreCheckoutQuery(AnswerPreCheckoutQuery answerPreCheckoutQuery) {
        telegramService.sendAnswerPreCheckoutQuery(answerPreCheckoutQuery);
    }

    @Override
    public void sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        try {
            telegramService.sendAnswerCallbackQuery(answerCallbackQuery);
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean isChatMember(String chatId, long userId) {
        GetChatMember isChatMember = new GetChatMember();
        isChatMember.setChatId(chatId);
        isChatMember.setUserId(userId);

        try {
            return BooleanUtils.toBoolean(telegramService.isChatMember(isChatMember));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Message sendMessage(SendMessage sendMessage) {
        return sendMessage(sendMessage, null);
    }

    @Override
    public Message sendMessage(SendMessage sendMessage, Object event) {
        return sendMessage0(sendMessage);
    }

    @Override
    public void removeInlineKeyboard(long chatId, Integer messageId) {
        if (messageId == null) {
            return;
        }
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);

        try {
            telegramService.editReplyMarkup(edit);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void editMessage(EditMessageText editMessageText) {
        if (editMessageText.getMessageId() == null) {
            return;
        }
        editMessageText.setParseMode(ParseMode.HTML);
        if (editMessageText.getDisableWebPagePreview() == null) {
            editMessageText.setDisableWebPagePreview(true);
        }

        telegramService.editMessageText(editMessageText);
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup) {
        if (editMessageReplyMarkup.getMessageId() == null) {
            return;
        }
        telegramService.editReplyMarkup(editMessageReplyMarkup);
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        if (editMessageCaption.getMessageId() == null) {
            return;
        }
        editMessageCaption.setParseMode(ParseMode.HTML);

        telegramService.editMessageCaption(editMessageCaption);
    }

    @Override
    public void deleteMessage(long chatId, Integer messageId) {
        if (messageId == null) {
            return;
        }
        try {
            telegramService.deleteMessage(new DeleteMessage(String.valueOf(chatId), messageId));
        } catch (Exception ignore) {
        }
    }

    @Override
    public void sendErrorMessage(long chatId, Locale locale) {
        sendMessage(SendMessage.builder().chatId(String.valueOf(chatId))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR, locale))
                .parseMode(ParseMode.HTML).build());
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice) {
        telegramService.sendInvoice(sendInvoice);
    }

    private Message sendMessage0(SendMessage sendMessage) {
        try {
            sendMessage.disableWebPagePreview();
            if (sendMessage.getDisableWebPagePreview() == null) {
                sendMessage.setAllowSendingWithoutReply(true);
            }

            return telegramService.sendMessage(sendMessage);
        } catch (Exception ex) {
            LOGGER.error("Error send message({})", sendMessage);
            throw ex;
        }
    }
}
