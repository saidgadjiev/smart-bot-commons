package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;

@Service
public class PaidSubscriptionRemoveService {

    private PaidSubscriptionDao paidSubscriptionDao;

    @Autowired
    public PaidSubscriptionRemoveService(@Redis PaidSubscriptionDao paidSubscriptionDao) {
        this.paidSubscriptionDao = paidSubscriptionDao;
    }

    public int removePaidSubscription(long userId) {
        return paidSubscriptionDao.remove(userId);
    }

    public void refreshPaidSubscription(long userId) {
        paidSubscriptionDao.refresh(userId);
    }

    public void refreshAllPaidSubscriptions() {
        paidSubscriptionDao.refreshAll();
    }
}
