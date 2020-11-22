package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.property.QueueProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

@Repository
public class WorkQueueDao extends QueueDao {

    private WorkQueueDaoDelegate queueDaoDelegate;

    @Autowired
    public WorkQueueDao(JdbcTemplate jdbcTemplate, WorkQueueDaoDelegate queueDaoDelegate, QueueProperties queueProperties) {
        super(jdbcTemplate, queueDaoDelegate, queueProperties);
        this.queueDaoDelegate = queueDaoDelegate;
    }

    public List<QueueItem> poll() {
        return queueDaoDelegate.poll();
    }

    public List<QueueItem> poll(int limit) {
        return queueDaoDelegate.poll(null, limit);
    }

    public List<QueueItem> poll(SmartExecutorService.JobWeight weight, int limit) {
        return queueDaoDelegate.poll(weight, limit);
    }

    public QueueItem getById(int id) {
        return queueDaoDelegate.getById(id);
    }

    public List<QueueItem> deleteAndGetProcessingOrWaitingByUserId(int userId) {
        return queueDaoDelegate.deleteAndGetProcessingOrWaitingByUserId(userId);
    }

    public QueueItem deleteAndGetById(int id) {
        return queueDaoDelegate.deleteAndGetById(id);
    }
}
