package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface WorkQueueDaoDelegate<T extends QueueItem> extends QueueDaoDelegate<T> {

    long countReadToComplete(SmartExecutorService.JobWeight weight);

    long countProcessing(SmartExecutorService.JobWeight weight);

    default List<T> poll(SmartExecutorService.JobWeight weight, int limit) {
        return Collections.emptyList();
    }

    default T getById(int id) {
        return null;
    }

    default List<T> deleteAndGetProcessingOrWaitingByUserId(int userId) {
        return Collections.emptyList();
    }

    default T deleteByIdAndStatusesAndGet(int id, Set<QueueItem.Status> statuses) {
        return null;
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
