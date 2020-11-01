package ru.gadjini.telegram.smart.bot.commons.service.queue;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;

import java.util.List;

@Service
public class QueueService {

    private QueueDao queueDao;

    @Autowired
    public QueueService(QueueDao queueDao) {
        this.queueDao = queueDao;
    }

    public List<QueueItem> poll(SmartExecutorService.JobWeight weight, int limit) {
        return queueDao.poll(weight, limit);
    }

    public void setWaiting(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        queueDao.setWaiting(id, exception);
    }

    public void setException(int id, Throwable ex) {
        String exception = ExceptionUtils.getMessage(ex) + "\n" + ExceptionUtils.getStackTrace(ex);
        queueDao.setException(id, exception);
    }

    public String getException(int id) {
        return queueDao.getException(id);
    }

    public void setProgressMessageId(int id, int progressMessageId) {
        queueDao.setProgressMessageId(id, progressMessageId);
    }

    public void setCompleted(int id) {
        queueDao.setCompleted(id);
    }

    public void setWaiting(int id) {
        queueDao.setWaiting(id);
    }

    public QueueItem getById(int id) {
        return queueDao.getById(id);
    }

    public void resetProcessing() {
        queueDao.resetProcessing();
    }

    public List<QueueItem> deleteAndGetProcessingOrWaitingByUserId(int userId) {
        return queueDao.deleteAndGetProcessingOrWaitingByUserId(userId);
    }

    public QueueItem deleteAndGetById(int id) {
        return queueDao.deleteAndGetById(id);
    }

    public void deleteById(int id) {
        queueDao.deleteById(id);
    }

    public boolean exists(int id) {
        return queueDao.exists(id);
    }
}
