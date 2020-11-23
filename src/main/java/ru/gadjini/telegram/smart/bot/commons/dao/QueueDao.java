package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class QueueDao {

    public static final String POLL_ORDER_BY = " ORDER BY qu.attempts, qu.id ";

    public static final String POLL_UPDATE_LIST = " status = 1, last_run_at = now(), attempts = attempts + 1, started_at = COALESCE(started_at, now()) ";

    public final void setExceptionStatus(int id, String exception) {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = ?, exception = ?, suppress_user_exceptions = TRUE WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.EXCEPTION.getCode());
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                }
        );
    }

    public final void setCompleted(int id) {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = ?, completed_at = now() WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.COMPLETED.getCode());
                    ps.setInt(2, id);
                }
        );
    }

    public final void setWaitingAndDecrementAttempts(int id) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0, attempts = GREATEST(0, attempts - 1) WHERE id = ?",
                ps -> ps.setInt(1, id));
    }

    public final void setWaiting(int id, ZonedDateTime nextRunAt, String exception) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET status = 0, next_run_at = ?, exception = ? WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Timestamp.valueOf(nextRunAt.toLocalDateTime()));
                    ps.setString(2, exception);
                    ps.setInt(3, id);
                });
    }

    public final void resetProcessing() {
        getJdbcTemplate().update(
                "UPDATE " + getQueueName() + " SET status = 0, attempts = GREATEST(0, attempts - 1) WHERE status = 1"
        );
    }

    public final void deleteById(int id) {
        getJdbcTemplate().update(
                "DELETE FROM " + getQueueName() + " WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }

    public final String getException(int id) {
        return getJdbcTemplate().query(
                "SELECT exception FROM " + getQueueName() + " WHERE id = ?",
                ps -> {
                    ps.setInt(1, id);
                },
                rs -> rs.next() ? rs.getString(QueueItem.EXCEPTION) : null
        );
    }

    public final boolean exists(int id) {
        return BooleanUtils.toBoolean(getJdbcTemplate().query(
                "SELECT TRUE FROM " + getQueueName() + " WHERE id =?",
                ps -> {
                    ps.setInt(1, id);
                },
                ResultSet::next
        ));
    }

    public final void setProgressMessageId(int id, int progressMessageId) {
        getJdbcTemplate().update("UPDATE " + getQueueName() + " SET progress_message_id = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, progressMessageId);
                    ps.setInt(2, id);
                });
    }

    public final long countByStatusForToday(QueueItem.Status status) {
        return getJdbcTemplate().query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ? AND created_at::date = current_date",
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final long countByStatusAllTime(QueueItem.Status status) {
        return getJdbcTemplate().query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ?",
                ps -> ps.setInt(1, status.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final Long countActiveUsersForToday() {
        return getJdbcTemplate().query(
                "SELECT count(DISTINCT user_id) as cnt FROM " + getQueueName() + " WHERE created_at::date = current_date",
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public final void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        getJdbcTemplate().update(
                "DELETE FROM " + getQueueName() + " WHERE id = ? AND status IN(" + statuses.stream()
                        .map(s -> String.valueOf(s.getCode())).collect(Collectors.joining(",")) + ")",
                ps -> ps.setInt(1, id)
        );
    }

    public final String getQueueName() {
        return getQueueDaoDelegate().getQueueName();
    }

    public abstract JdbcTemplate getJdbcTemplate();

    public abstract QueueDaoDelegate getQueueDaoDelegate();
}
