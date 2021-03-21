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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiService;

import java.util.Locale;
import java.util.function.Consumer;

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
    public boolean isChatMember(String chatId, int userId) {
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
    public void sendMessage(SendMessage sendMessage) {
        sendMessage(sendMessage, null);
    }

    @Override
    public void sendMessage(SendMessage sendMessage, Consumer<Message> callback) {
        sendMessage0(sendMessage, callback);
    }

    @Override
    public void removeInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);

        try {
            telegramService.editReplyMarkup(edit);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void editMessage(EditMessageText editMessageText, boolean ignoreException) {
        editMessageText.setParseMode(ParseMode.HTML);

        try {
            telegramService.editMessageText(editMessageText);
        } catch (Exception ex) {
            if (!ignoreException) {
                throw ex;
            }
        }
    }

    @Override
    public void editKeyboard(EditMessageReplyMarkup editMessageReplyMarkup, boolean ignoreException) {
        try {
            telegramService.editReplyMarkup(editMessageReplyMarkup);
        } catch (Exception ex) {
            if (!ignoreException) {
                throw ex;
            }
        }
    }

    @Override
    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        editMessageCaption.setParseMode(ParseMode.HTML);

        telegramService.editMessageCaption(editMessageCaption);
    }

    @Override
    public void deleteMessage(long chatId, int messageId) {
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
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale) {
        sendMessage(SendMessage.builder().chatId(String.valueOf(chatId)).text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED, locale))
                .parseMode(ParseMode.HTML)
                .replyMarkup(replyKeyboard).build());
    }

    @Override
    public void sendInvoice(SendInvoice sendInvoice, Consumer<Message> callback) {
        Message message = telegramService.sendInvoice(sendInvoice);

        if (callback != null) {
            callback.accept(message);
        }
    }

    private void sendMessage0(SendMessage sendMessage, Consumer<Message> callback) {
        try {
            sendMessage.disableWebPagePreview();
            sendMessage.setAllowSendingWithoutReply(true);
            Message message = telegramService.sendMessage(sendMessage);

            if (callback != null) {
                callback.accept(message);
            }
        } catch (TelegramApiRequestException ex) {
            LOGGER.error("Error send message({})", sendMessage);
            throw ex;
        }
    }
}
