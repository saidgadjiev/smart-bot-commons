package ru.gadjini.telegram.smart.bot.commons.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.job.TgMethodExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;
import java.util.function.Consumer;

@Service
@Qualifier("asyncMessage")
public class AsyncMessageService implements MessageService {

    private LocalisationService localisationService;

    private TgMethodExecutor messageSenderJob;

    private MessageService messageService;

    @Autowired
    public AsyncMessageService(LocalisationService localisationService, TgMethodExecutor messageSenderJob,
                               @Qualifier("message") MessageService messageService) {
        this.localisationService = localisationService;
        this.messageSenderJob = messageSenderJob;
        this.messageService = messageService;
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
        messageSenderJob.push(() -> messageService.editMessage(editMessageText, false));
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
        sendMessage(SendMessage.builder().chatId(String.valueOf(chatId))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_ERROR, locale))
                .parseMode(ParseMode.HTML)
                .build());
    }

    @Override
    public void sendBotRestartedMessage(long chatId, ReplyKeyboard replyKeyboard, Locale locale) {
        sendMessage(SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED, locale))
                .replyMarkup(replyKeyboard).build());
    }
}
