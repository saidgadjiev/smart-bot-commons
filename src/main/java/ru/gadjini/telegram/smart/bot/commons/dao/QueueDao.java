package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class QueueDao {

    private JdbcTemplate jdbcTemplate;

    private QueueDaoDelegate queueDaoDelegate;

    @Autowired
    public QueueDao(JdbcTemplate jdbcTemplate, QueueDaoDelegate queueDaoDelegate) {
        this.jdbcTemplate = jdbcTemplate;
        this.queueDaoDelegate = queueDaoDelegate;
    }

    public void setException(int id, String exception) {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET status = ?, exception = ?, suppress_user_exceptions = TRUE WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.EXCEPTION.getCode());
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                }
        );
    }

    public void setCompleted(int id) {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET status = ?, completed_at = now() WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.COMPLETED.getCode());
                    ps.setInt(2, id);
                }
        );
    }

    public void setWaiting(int id) {
        jdbcTemplate.update("UPDATE " + getQueueName() + " SET status = 0 WHERE id = ?",
                ps -> ps.setInt(1, id));
    }

    public void setWaiting(int id, String exception) {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET exception = ?, status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, exception);
                    ps.setInt(2, QueueItem.Status.WAITING.getCode());
                    ps.setInt(3, id);
                }
        );
    }

    public void resetProcessing() {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET status = 0 WHERE status = 1"
        );
    }

    public void deleteById(int id) {
        jdbcTemplate.update(
                "DELETE FROM " + getQueueName() + " WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    public String getException(int id) {
        return jdbcTemplate.query(
                "SELECT exception FROM " + getQueueName() + " WHERE id = ?",
                ps -> {
                    ps.setInt(1, id);
                },
                rs -> rs.next() ? rs.getString(QueueItem.EXCEPTION) : null
        );
    }

    public boolean exists(int id) {
        return BooleanUtils.toBoolean(jdbcTemplate.query(
                "SELECT TRUE FROM " + getQueueName() + " WHERE id =?",
                ps -> {
                    ps.setInt(1, id);
                },
                ResultSet::next
        ));
    }

    public void setProgressMessageId(int id, int progressMessageId) {
        jdbcTemplate.update("UPDATE " + getQueueName() + " SET progress_message_id = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, progressMessageId);
                    ps.setInt(2, id);
                });
    }

    public void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        jdbcTemplate.update(
                "DELETE FROM " + getQueueName() + " WHERE id = ? AND status IN(" + statuses.stream()
                        .map(s -> String.valueOf(s.getCode())).collect(Collectors.joining(",")) + ")",
                ps -> ps.setInt(1, id)
        );
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

    private String getQueueName() {
        return queueDaoDelegate.getQueueName();
    }
}
