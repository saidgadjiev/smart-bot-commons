package ru.gadjini.telegram.smart.bot.commons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.SubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.common.CommonConstants;

import java.util.concurrent.TimeUnit;

@Service
public class SubscriptionService {

    private SubscriptionDao subscriptionDao;

    @Autowired
    public SubscriptionService(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public boolean isChatMember(int userId) {
        return subscriptionDao.isChatMember(CommonConstants.SMART_FILE_UTILS_CHANNEL, userId, 1, TimeUnit.DAYS);
    }
}
