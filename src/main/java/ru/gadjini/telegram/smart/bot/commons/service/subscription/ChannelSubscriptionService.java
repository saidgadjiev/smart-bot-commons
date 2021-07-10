package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.channel.ChannelSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.common.CommonConstants;

import java.util.concurrent.TimeUnit;

@Service
public class ChannelSubscriptionService {

    private ChannelSubscriptionDao subscriptionDao;

    @Autowired
    public ChannelSubscriptionService(ChannelSubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public boolean isSubscriptionExists(long userId) {
        return subscriptionDao.isChatMember(CommonConstants.SMART_FILE_UTILS_CHANNEL, userId, 1, TimeUnit.DAYS);
    }
}
