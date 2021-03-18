package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Caching;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class PaidSubscriptionService {

    private BotProperties botProperties;

    private PaidSubscriptionDao paidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public PaidSubscriptionService(BotProperties botProperties, @Caching PaidSubscriptionDao paidSubscriptionDao,
                                   SubscriptionProperties subscriptionProperties) {
        this.botProperties = botProperties;
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
    }

    public PaidSubscription getSubscription(int userId) {
        return paidSubscriptionDao.getPaidSubscription(botProperties.getName(), userId);
    }

    public void createTrialSubscription(int userId) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setEndDate(LocalDate.now(ZoneOffset.UTC).plusDays(subscriptionProperties.getTrialPeriod()));

        paidSubscriptionDao.create(paidSubscription);
    }
}
