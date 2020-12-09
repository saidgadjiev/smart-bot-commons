package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.Collections;
import java.util.List;

public interface WorkQueueDaoDelegate<T extends QueueItem> extends QueueDaoDelegate<T> {

    default List<T> poll() {
        return Collections.emptyList();
    }

    default List<T> poll(SmartExecutorService.JobWeight weight, int limit) {
        return Collections.emptyList();
    }

    default T getById(int id) {
         return null;
    }

    default List<T> deleteAndGetProcessingOrWaitingByUserId(int userId) {
         return Collections.emptyList();
    }

    default T deleteAndGetById(int id) {
         return null;
    }

    default boolean isDeleteCompletedShouldBeDelegated() {
        return false;
    }

    default List<T> deleteCompleted() {
        throw new UnsupportedOperationException();
    }

    String getProducerName();
}
