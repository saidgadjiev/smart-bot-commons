package ru.gadjini.telegram.smart.bot.commons.service.queue;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

public interface QueueJobConfigurator {

    boolean isNeedUpdateMessageAfterCancel(QueueItem queueItem);
}
