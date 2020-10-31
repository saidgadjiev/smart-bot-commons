package ru.gadjini.telegram.smart.bot.commons.dao;

import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

public interface QueueDaoDelegate {
    
     List<QueueItem> poll(SmartExecutorService.JobWeight weight, int limit);

     QueueItem getById(int id);

     List<QueueItem> deleteAndGetProcessingOrWaitingByUserId(int userId);

     QueueItem deleteAndGetById(int id);

     String getQueueName();
}
