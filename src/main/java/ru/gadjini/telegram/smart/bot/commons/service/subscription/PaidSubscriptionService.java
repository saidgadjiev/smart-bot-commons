package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Caching;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class PaidSubscriptionService {

    public static final DateTimeFormatter PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private PaidSubscriptionDao paidSubscriptionDao;

    private SubscriptionProperties subscriptionProperties;

    @Autowired
    public PaidSubscriptionService(@Caching PaidSubscriptionDao paidSubscriptionDao,
                                   SubscriptionProperties subscriptionProperties) {
        this.paidSubscriptionDao = paidSubscriptionDao;
        this.subscriptionProperties = subscriptionProperties;
    }

    public PaidSubscription getSubscription(String botName, int userId) {
        return paidSubscriptionDao.getByBotNameAndUserId(botName, userId);
    }

    public LocalDate createTrialSubscription(String botName, int userId) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setBotName(botName);
        paidSubscription.setEndDate(getTrialPeriodEndDate());

        paidSubscriptionDao.create(paidSubscription);

        return paidSubscription.getEndDate();
    }

    public LocalDate renewSubscription(String botName, int userId, int planId, Period period) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setBotName(botName);
        paidSubscription.setPlanId(planId);
        paidSubscription.setEndDate(JodaTimeUtils.plus(LocalDate.now(ZoneOffset.UTC), period));

        paidSubscriptionDao.createOrRenew(paidSubscription, period);

        return paidSubscription.getEndDate();
    }

    private LocalDate getTrialPeriodEndDate() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(subscriptionProperties.getTrialPeriod());
    }
}
