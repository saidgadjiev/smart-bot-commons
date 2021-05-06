package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;

@Service
public class PaidSubscriptionRemoveService {

    private PaidSubscriptionDao paidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public PaidSubscriptionRemoveService(@Redis PaidSubscriptionDao paidSubscriptionDao, SubscriptionProperties subscriptionProperties) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
    }

    public int removePaidSubscription(int userId) {
        return paidSubscriptionDao.remove(subscriptionProperties.getPaidBotName(), userId);
    }
}
