package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Qualifier("fixed")
public class FixedTariffPaidSubscriptionService implements PaidSubscriptionService {

    public static final DateTimeFormatter HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy '<i>'z'</i>'");

    public static final DateTimeFormatter PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy z");

    private PaidSubscriptionDao paidSubscriptionDao;

    private PaidSubscriptionDao dbPaidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public FixedTariffPaidSubscriptionService(@Redis PaidSubscriptionDao paidSubscriptionDao,
                                              @DB PaidSubscriptionDao dbPaidSubscriptionDao,
                                              SubscriptionProperties subscriptionProperties) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
        this.dbPaidSubscriptionDao = dbPaidSubscriptionDao;
    }

    @Override
    public boolean isExistsPaidSubscription(String botName, long userId) {
        PaidSubscription subscription = paidSubscriptionDao.getByUserId(userId);

        return subscription != null && subscription.getPlanId() != null && subscription.isActive();
    }

    public PaidSubscription getSubscriptionWithoutCache(long userId) {
        return dbPaidSubscriptionDao.getByUserId(userId);
    }

    public PaidSubscription getSubscription(long userId) {
        return paidSubscriptionDao.getByUserId(userId);
    }

    public PaidSubscription createTrialSubscription(long userId) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setEndDate(getTrialPeriodEndDate());

        paidSubscriptionDao.create(paidSubscription);

        return paidSubscription;
    }

    @Override
    public PaidSubscription renewSubscription(long userId, int planId, Period period) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setPlanId(planId);
        //Если у пользователя еще нет никакой подписки, то добавляем еще пробный период
        paidSubscription.setEndDate(JodaTimeUtils.plus(LocalDate.now(TimeUtils.UTC)
                .plusDays(subscriptionProperties.getTrialPeriod()), period));

        paidSubscriptionDao.createOrRenew(paidSubscription, period);

        return paidSubscription;
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }

    private LocalDate getTrialPeriodEndDate() {
        return LocalDate.now(TimeUtils.UTC).plusDays(subscriptionProperties.getTrialPeriod());
    }
}
