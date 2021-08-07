package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

@Service
@Qualifier("flexible")
public class FlexibleTariffPaidSubscriptionService implements PaidSubscriptionService {

    private PaidSubscriptionDao paidSubscriptionDao;

    @Autowired
    public FlexibleTariffPaidSubscriptionService(@Redis PaidSubscriptionDao paidSubscriptionDao) {
        this.paidSubscriptionDao = paidSubscriptionDao;
    }

    @Override
    public boolean isExistsPaidSubscription(String botName, long userId) {
        PaidSubscription subscription = paidSubscriptionDao.getByBotNameAndUserId(userId);

        return subscription != null && subscription.getPlanId() != null && (subscription.isActive() || subscription.isSubscriptionIntervalActive());
    }

    @Override
    public PaidSubscription renewSubscription(long userId, int planId, Period period) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setPlanId(planId);

        paidSubscription.setSubscriptionInterval(period);

        paidSubscriptionDao.createOrRenew(paidSubscription, period);

        return paidSubscription;
    }

    public PaidSubscription activateSubscriptionDay(long userId) {
        return paidSubscriptionDao.activateSubscriptionDay(userId);
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}