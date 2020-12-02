package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class WorkQueueDao extends QueueDao {

    private final JdbcTemplate jdbcTemplate;

    private WorkQueueDaoDelegate queueDaoDelegate;

    @Autowired
    public WorkQueueDao(JdbcTemplate jdbcTemplate, WorkQueueDaoDelegate queueDaoDelegate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public List<QueueItem> deleteCompleted() {
        if (queueDaoDelegate.isDeleteCompletedShouldBeDelegated()) {
            return queueDaoDelegate.deleteCompleted();
        }

        return doDeleteCompleted();
    }

    public List<QueueItem> doDeleteCompleted() {
        return jdbcTemplate.query(
                "WITH del AS(DELETE FROM " + getQueueName() + " WHERE status = ? AND completed_at + " + DELETE_COMPLETED_INTERVAL + " < now() RETURNING *)" +
                        "SELECT * FROM del",
                ps -> ps.setInt(1, QueueItem.Status.COMPLETED.getCode()),
                (rs, rowNum) -> map(rs)
        );
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public QueueDaoDelegate getQueueDaoDelegate() {
        return queueDaoDelegate;
    }

    private QueueItem map(ResultSet rs) throws SQLException {
        QueueItem queueItem = new QueueItem();
        queueItem.setId(rs.getInt(QueueItem.ID));
        queueItem.setUserId(rs.getInt(QueueItem.USER_ID));

        return queueItem;
    }
}
