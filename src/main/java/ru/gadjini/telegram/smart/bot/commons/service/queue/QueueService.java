package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;

import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class QueueService {

    private QueueDao queueDao;

    public QueueService(QueueDao queueDao) {
        this.queueDao = queueDao;
    }

    public final void setWaitingIfThereAreAttemptsElseException(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        queueDao.setWaitingIfThereAreAttemptsElseException(id, exception);
    }

    public final void setExceptionStatus(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        queueDao.setExceptionStatus(id, exception);
    }

    public final String getException(int id) {
        return queueDao.getException(id);
    }

    public final void setProgressMessageId(int id, int progressMessageId) {
        queueDao.setProgressMessageId(id, progressMessageId);
    }

    public final void setCompleted(int id) {
        queueDao.setCompleted(id);
    }

    public final void setWaitingAndDecrementAttempts(int id) {
        queueDao.setWaitingAndDecrementAttempts(id);
    }

    public final void setWaiting(int id, ZonedDateTime nextRunAt, Throwable reason) {
        String exception = ExceptionUtils.getMessage(reason) + "\n" + ExceptionUtils.getStackTrace(reason);
        queueDao.setWaiting(id, nextRunAt, exception);
    }

    public final long countByStatusAllTime(QueueItem.Status status) {
        return queueDao.countByStatusAllTime(status);
    }

    public final long countByStatusForToday(QueueItem.Status status) {
        return queueDao.countByStatusForToday(status);
    }

    public final long countActiveUsersForToday() {
        return queueDao.countActiveUsersForToday();
    }

    public final void resetProcessing() {
        queueDao.resetProcessing();
    }

    public final void deleteByIdAndStatuses(int id, Set<QueueItem.Status> statuses) {
        queueDao.deleteByIdAndStatuses(id, statuses);
    }

    public final void deleteById(int id) {
        queueDao.deleteById(id);
    }

    public final boolean exists(int id) {
        return queueDao.exists(id);
    }
}
