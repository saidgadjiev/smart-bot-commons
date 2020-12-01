package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

import java.util.Locale;

public interface QueueJobConfigurator<T extends QueueItem> {

    default boolean isNeedUpdateMessageAfterCancel(T queueItem) {
        return true;
    }

    String getWaitingMessage(T queueItem, Locale locale);

    InlineKeyboardMarkup getWaitingKeyboard(T queueItem, Locale locale);

    default String getErrorCode(Throwable e) {
        return null;
    }
}
