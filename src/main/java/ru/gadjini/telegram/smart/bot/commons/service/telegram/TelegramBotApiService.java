package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import ru.gadjini.telegram.smart.bot.commons.annotation.botapi.TelegramBotApi;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;

import java.util.Set;

@Service
public class TelegramBotApiService extends DefaultAbsSender {

    private final BotProperties botProperties;

    private TelegramBotApiMethodExecutor exceptionHandler;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties,
                                 @TelegramBotApi DefaultBotOptions botOptions,
                                 TelegramBotApiMethodExecutor exceptionHandler) {
        super(botOptions);
        this.botProperties = botProperties;
        this.exceptionHandler = exceptionHandler;
    }

    public Boolean isChatMember(GetChatMember isChatMember) {
        return exceptionHandler.executeWithResult(null, () -> {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(isChatMember.getChatId());
            getChatMember.setUserId(isChatMember.getUserId());

            ChatMember member = execute(getChatMember);

            return isInGroup(member.getStatus());
        });
    }

    public Boolean sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        return exceptionHandler.executeWithResult(null, () -> execute(answerCallbackQuery));
    }

    public Boolean sendAnswerPreCheckoutQuery(AnswerPreCheckoutQuery answerPreCheckoutQuery) {
        return exceptionHandler.executeWithResult(null, () -> execute(answerPreCheckoutQuery));
    }

    public Message sendMessage(SendMessage sendMessage) {
        return exceptionHandler.executeWithResult(sendMessage.getChatId(), () -> execute(sendMessage));
    }

    public void editReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        exceptionHandler.executeWithoutResult(editMessageReplyMarkup.getChatId(), () -> {
            execute(editMessageReplyMarkup);
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        exceptionHandler.executeWithoutResult(editMessageText.getChatId(), () -> {
            execute(editMessageText);
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        exceptionHandler.executeWithoutResult(editMessageCaption.getChatId(), () -> {
            execute(editMessageCaption);
        });
    }

    public Boolean deleteMessage(DeleteMessage deleteMessage) {
        return exceptionHandler.executeWithResult(deleteMessage.getChatId(), () -> {
            return execute(deleteMessage);
        });
    }

    public Message sendInvoice(SendInvoice sendInvoice) {
        return exceptionHandler.executeWithResult(String.valueOf(sendInvoice.getChatId()), () -> {
            return execute(sendInvoice);
        });
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    private boolean isInGroup(String status) {
        if (StringUtils.isBlank(status)) {
            return true;
        }
        return Set.of("creator", "administrator", "member", "restricted").contains(status);
    }
}
