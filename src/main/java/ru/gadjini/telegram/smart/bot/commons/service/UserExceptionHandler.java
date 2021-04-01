package ru.gadjini.telegram.smart.bot.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
public class UserExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserExceptionHandler.class);

    private MessageService messageService;

    @Autowired
    public UserExceptionHandler(@TgMessageLimitsControl MessageService messageService) {
        this.messageService = messageService;
    }

    public void handle(Update update, UserException ex) {
        if (ex.isPrintLog()) {
            LOGGER.error(ex.getMessage(), ex);
        }
        if (ex.isAnswerPreCheckout()) {
            AnswerPreCheckoutQuery answerPreCheckoutQuery = AnswerPreCheckoutQuery.builder()
                    .ok(false)
                    .errorMessage(ex.getHumanMessage())
                    .preCheckoutQueryId(ex.getPreCheckoutQueryId())
                    .build();
            messageService.sendAnswerPreCheckoutQuery(answerPreCheckoutQuery);
        } else {
            messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(TgMessage.getChatId(update)))
                    .parseMode(ParseMode.HTML)
                    .text(ex.getHumanMessage()).replyToMessageId(ex.getReplyToMessageId()).build());
        }
    }
}
