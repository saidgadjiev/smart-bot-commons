package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class FatherPaidSubscriptionService {

    private PaidSubscriptionDao paidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    private PaidSubscriptionPlanService paidSubscriptionPlanService;

    private Map<PaidSubscriptionTariffType, PaidSubscriptionService> paidSubscriptionServiceMap;

    @Autowired
    public FatherPaidSubscriptionService(@Redis PaidSubscriptionDao paidSubscriptionDao,
                                         SubscriptionProperties subscriptionProperties,
                                         PaidSubscriptionPlanService paidSubscriptionPlanService,
                                         Map<PaidSubscriptionTariffType, PaidSubscriptionService> paidSubscriptionServiceMap) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
        this.paidSubscriptionPlanService = paidSubscriptionPlanService;
        this.paidSubscriptionServiceMap = paidSubscriptionServiceMap;
    }

    public boolean isSubscriptionExpired(long userId) {
        PaidSubscription subscription = getSubscription(userId);

        if (subscription == null) {
            return true;
        }
        PaidSubscriptionTariffType tariff = paidSubscriptionPlanService.getTariff(subscription.getPlanId());

        if (tariff == null) {
            return true;
        }

        return paidSubscriptionServiceMap.get(tariff).isExpired(subscription);
    }

    public boolean isSubscriptionPeriodActive(PaidSubscription paidSubscription) {
        if (paidSubscription == null) {
            return false;
        }

        PaidSubscriptionTariffType tariff = paidSubscriptionPlanService.getTariff(paidSubscription.getPlanId());

        if (tariff == null) {
            return false;
        }

        return paidSubscriptionServiceMap.get(tariff).isSubscriptionPeriodActive(paidSubscription);
    }

    public PaidSubscription getSubscription(long userId) {
        return paidSubscriptionDao.getByUserId(userId);
    }

    public PaidSubscription createTrialSubscription(long userId) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setEndAt(getTrialPeriodEndDate());

        paidSubscriptionDao.create(paidSubscription);

        return paidSubscription;
    }

    private ZonedDateTime getTrialPeriodEndDate() {
        return ZonedDateTime.now(TimeUtils.UTC).plusDays(subscriptionProperties.getTrialPeriod());
    }
}
