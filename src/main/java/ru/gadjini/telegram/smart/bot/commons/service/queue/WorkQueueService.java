package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

@Service
public class WorkQueueService extends QueueService {

    private final WorkQueueDao queueDao;

    @Autowired
    public WorkQueueService(WorkQueueDao workQueueDao) {
        this.queueDao = workQueueDao;
    }

    public List<QueueItem> poll() {
        return queueDao.poll();
    }

    public List<QueueItem> poll(int limit) {
        return queueDao.poll(limit);
    }

    public List<QueueItem> poll(SmartExecutorService.JobWeight weight, int limit) {
        return queueDao.poll(weight, limit);
    }

    public QueueItem getById(int id) {
        return queueDao.getById(id);
    }

    public List<QueueItem> deleteAndGetProcessingOrWaitingByUserId(int userId) {
        return queueDao.deleteAndGetProcessingOrWaitingByUserId(userId);
    }

    public QueueItem deleteAndGetById(int id) {
        return queueDao.deleteAndGetById(id);
    }

    @Override
    public QueueDao getQueueDao() {
        return queueDao;
    }
}
