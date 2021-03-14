package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;

import java.util.Set;

@Service
public class TelegramBotApiService extends DefaultAbsSender {

    private final BotProperties botProperties;

    private ObjectMapper objectMapper;

    private TelegramBotApiMethodExecutor exceptionHandler;

    @Autowired
    public TelegramBotApiService(BotProperties botProperties, ObjectMapper objectMapper,
                                 DefaultBotOptions botOptions, TelegramBotApiMethodExecutor exceptionHandler) {
        super(botOptions);
        this.botProperties = botProperties;
        this.objectMapper = objectMapper;
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
        return exceptionHandler.executeWithResult(null, () -> {
            return execute(objectMapper.convertValue(answerCallbackQuery, AnswerCallbackQuery.class));
        });
    }

    public Message sendMessage(SendMessage sendMessage) {
        return exceptionHandler.executeWithResult(sendMessage.getChatId(), () -> {
            Message execute = execute(objectMapper.convertValue(sendMessage, SendMessage.class));

            return objectMapper.convertValue(execute, Message.class);
        });
    }

    public void editReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        exceptionHandler.executeWithoutResult(editMessageReplyMarkup.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageReplyMarkup, EditMessageReplyMarkup.class));
        });
    }

    public void editMessageText(EditMessageText editMessageText) {
        exceptionHandler.executeWithoutResult(editMessageText.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageText, EditMessageText.class));
        });
    }

    public void editMessageCaption(EditMessageCaption editMessageCaption) {
        exceptionHandler.executeWithoutResult(editMessageCaption.getChatId(), () -> {
            execute(objectMapper.convertValue(editMessageCaption, EditMessageCaption.class));
        });
    }

    public Boolean deleteMessage(DeleteMessage deleteMessage) {
        return exceptionHandler.executeWithResult(deleteMessage.getChatId(), () -> {
            return execute(objectMapper.convertValue(deleteMessage, DeleteMessage.class));
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
