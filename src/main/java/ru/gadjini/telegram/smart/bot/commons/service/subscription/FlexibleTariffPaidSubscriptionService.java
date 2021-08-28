package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.controller.api.PaidSubscriptionApi;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;

@Service
@Qualifier("flexible")
public class FlexibleTariffPaidSubscriptionService implements PaidSubscriptionService {

    private PaidSubscriptionDao paidSubscriptionDao;

    private PaidSubscriptionApi paidSubscriptionApi;

    private SubscriptionProperties subscriptionProperties;

    private BotProperties botProperties;

    @Autowired
    public FlexibleTariffPaidSubscriptionService(@Redis PaidSubscriptionDao paidSubscriptionDao,
                                                 PaidSubscriptionApi paidSubscriptionApi,
                                                 SubscriptionProperties subscriptionProperties,
                                                 BotProperties botProperties) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.paidSubscriptionApi = paidSubscriptionApi;
        this.subscriptionProperties = subscriptionProperties;
        this.botProperties = botProperties;
    }

    @Override
    public boolean isExpired(PaidSubscription paidSubscription) {
        return !isSubscriptionPeriodActive(paidSubscription) || JodaTimeUtils.toDays(paidSubscription.getSubscriptionInterval()) <= 0;
    }

    @Override
    public boolean isSubscriptionPeriodActive(PaidSubscription paidSubscription) {
        if (paidSubscription.getSubscriptionInterval() == null
                || paidSubscription.getEndAt() == null) {
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now(TimeUtils.UTC);

        return now.isBefore(paidSubscription.getEndAt()) || now.isEqual(paidSubscription.getEndAt());
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
        PaidSubscription paidSubscription = paidSubscriptionDao.activateSubscriptionDay(userId);

        if (paidSubscription != null) {
            paidSubscriptionApi.refreshPaidSubscription(subscriptionProperties.getPaymentBotServer(),
                    userId, botProperties.getName());
        }

        return paidSubscription;
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FLEXIBLE;
    }
}
