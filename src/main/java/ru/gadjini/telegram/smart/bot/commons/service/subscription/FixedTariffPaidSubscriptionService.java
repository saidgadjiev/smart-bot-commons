package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Qualifier("fixed")
public class FixedTariffPaidSubscriptionService implements PaidSubscriptionService {

    public static final DateTimeFormatter HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy '<i>'z'</i>'");

    public static final DateTimeFormatter PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy z");

    private PaidSubscriptionDao paidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public FixedTariffPaidSubscriptionService(@Redis PaidSubscriptionDao paidSubscriptionDao,
                                              SubscriptionProperties subscriptionProperties) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
    }

    @Override
    public boolean isExpired(PaidSubscription paidSubscription) {
        return !isSubscriptionPeriodActive(paidSubscription);
    }

    @Override
    public boolean isSubscriptionPeriodActive(PaidSubscription paidSubscription) {
        if (paidSubscription.getEndAt() == null) {
            return false;
        }
        LocalDate now = LocalDate.now(TimeUtils.UTC);
        LocalDate endAt = paidSubscription.getEndAt().toLocalDate();

        return now.isBefore(endAt) || now.isEqual(endAt);
    }

    @Override
    public PaidSubscription renewSubscription(long userId, int planId, Period period) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setPlanId(planId);
        //Если у пользователя еще нет никакой подписки, то добавляем еще пробный период
        paidSubscription.setEndAt(JodaTimeUtils.plus(ZonedDateTime.now(TimeUtils.UTC)
                .plusDays(subscriptionProperties.getTrialPeriod()), period));

        paidSubscriptionDao.createOrRenew(paidSubscription, period);

        return paidSubscription;
    }

    @Override
    public PaidSubscriptionTariffType tariffType() {
        return PaidSubscriptionTariffType.FIXED;
    }
}
