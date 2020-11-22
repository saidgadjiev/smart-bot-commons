package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.property.QueueProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class QueueDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueDao.class);

    public static final String POLL_ORDER_BY = " ORDER BY qu.attempts, qu.id ";

    public static final String POLL_UPDATE_LIST = " status = 1, last_run_at = now(), attempts = attempts + 1, started_at = COALESCE(started_at, now()) ";

    private JdbcTemplate jdbcTemplate;

    private QueueDaoDelegate queueDaoDelegate;

    private QueueProperties queueProperties;

    @Autowired
    public QueueDao(JdbcTemplate jdbcTemplate, QueueDaoDelegate queueDaoDelegate, QueueProperties queueProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.queueDaoDelegate = queueDaoDelegate;
        this.queueProperties = queueProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Max attempts({})", queueProperties.getMaxAttempts());
    }

    public void setExceptionStatus(int id, String exception) {
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

    public void setWaitingAndDecrementAttempts(int id) {
        jdbcTemplate.update("UPDATE " + getQueueName() + " SET status = 0, attempts = GREATEST(0, attempts - 1) WHERE id = ?",
                ps -> ps.setInt(1, id));
    }


    public void setWaiting(int id, ZonedDateTime nextRunAt, String exception) {
        jdbcTemplate.update("UPDATE " + getQueueName() + " SET status = 0, next_run_at = ?, exception = ? WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(nextRunAt.toLocalDateTime()));
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                });
    }

    public void setWaitingIfThereAreAttemptsElseException(int id, String exception) {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET exception = ?, status = case when attempts < ? then 0 else 2 end WHERE id = ?",
                ps -> {
                    ps.setString(1, exception);
                    ps.setInt(2, queueProperties.getMaxAttempts());
                    ps.setInt(3, id);
                }
        );
    }

    public void resetProcessing() {
        jdbcTemplate.update(
                "UPDATE " + getQueueName() + " SET status = 0, attempts = GREATEST(0, attempts - 1) WHERE status = 1"
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

    public long countByStatusForToday(QueueItem.Status status) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ? AND created_at::date = current_date",
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public long countByStatusAllTime(QueueItem.Status status) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ?",
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public Long countActiveUsersForToday() {
        return jdbcTemplate.query(
                "SELECT count(DISTINCT user_id) as cnt FROM " + getQueueName() + " WHERE created_at::date = current_date",
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        jdbcTemplate.update(
                "DELETE FROM " + getQueueName() + " WHERE id = ? AND status IN(" + statuses.stream()
                        .map(s -> String.valueOf(s.getCode())).collect(Collectors.joining(",")) + ")",
                ps -> ps.setInt(1, id)
        );
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

    public String getQueueName() {
        return queueDaoDelegate.getQueueName();
    }
}
