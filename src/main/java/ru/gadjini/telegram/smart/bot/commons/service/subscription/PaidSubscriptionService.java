package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.annotation.Caching;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.PaidSubscriptionDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class PaidSubscriptionService {

    public static final DateTimeFormatter PAID_SUBSCRIPTION_END_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

    public LocalDate createTrialSubscription(int userId) {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setBotName(botProperties.getName());
        paidSubscription.setEndDate(LocalDate.now(ZoneOffset.UTC).plusDays(subscriptionProperties.getTrialPeriod()));

        paidSubscriptionDao.create(paidSubscription);

        return paidSubscription.getEndDate();
    }

    public LocalDate renewSubscription(int userId, int planId, Period period) {
        return paidSubscriptionDao.updateEndDate(botProperties.getName(), userId, planId, period);
    }
}
