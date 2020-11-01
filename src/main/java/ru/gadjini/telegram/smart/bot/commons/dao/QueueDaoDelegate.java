package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

public interface QueueDaoDelegate<T extends QueueItem> {
    
     List<T> poll(SmartExecutorService.JobWeight weight, int limit);

     T getById(int id);

     List<T> deleteAndGetProcessingOrWaitingByUserId(int userId);

     T deleteAndGetById(int id);

     String getQueueName();
}
