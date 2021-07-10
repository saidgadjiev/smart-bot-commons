package ru.gadjini.telegram.smart.bot.commons.service.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.BulkDistributionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.BulkDistribution;

@Component
public class BulkDistributionService {

    private BulkDistributionDao bulkDistributionDao;

    @Autowired
    public BulkDistributionService(BulkDistributionDao bulkDistributionDao) {
        this.bulkDistributionDao = bulkDistributionDao;
    }

    public synchronized BulkDistribution getDistribution(String botName) {
        return bulkDistributionDao.getFirstDistribution(botName);
    }

    public synchronized BulkDistribution deleteAndGetDistribution(long userId, String botName) {
        return bulkDistributionDao.deleteAndGet(userId, botName);
    }

    public synchronized void delete(int id) {
        bulkDistributionDao.delete(id);
    }
}
