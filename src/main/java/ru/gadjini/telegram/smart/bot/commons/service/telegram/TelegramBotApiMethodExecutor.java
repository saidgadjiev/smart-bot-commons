package ru.gadjini.telegram.smart.bot.commons.service.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.model.web.HttpCodes;

@Component
public class TelegramBotApiMethodExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotApiMethodExecutor.class);

    void executeWithoutResult(String chatId, Executable executable) {
        try {
            executable.executeWithException();
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    <V> V executeWithResult(String chatId, Callable<V> executable) {
        try {
            return executable.executeWithResult();
        } catch (Exception e) {
            throw catchException(chatId, e);
        }
    }

    private RuntimeException catchException(String chatId, Exception ex) {
        if (ex instanceof org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) {
            org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException e = (org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException) ex;
            LOGGER.error("(" + chatId + ")" + e.getMessage() + "\n" + e.getErrorCode() + "\n" + e.getApiResponse(), e);
            if (e.getErrorCode() == HttpCodes.TOO_MANY_REQUESTS) {
                return new FloodWaitException(e.getApiResponse(), 30);
            }
            return new TelegramApiRequestException(chatId, e.getMessage(), e.getErrorCode(), e.getApiResponse(), e);
        } else {
            LOGGER.error("(" + chatId + ")" + ex.getMessage(), ex);
            return new TelegramApiException(ex);
        }
    }

    public interface Executable {

        void executeWithException() throws Exception;
    }

    public interface Callable<V> {

        V executeWithResult() throws Exception;
    }
}
