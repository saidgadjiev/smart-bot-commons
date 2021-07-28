package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.PaidSubscriptionPlanDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.List;

@Service
public class PaidSubscriptionPlanService {

    private PaidSubscriptionPlanDao paidSubscriptionPlanDao;

    @Autowired
    public PaidSubscriptionPlanService(PaidSubscriptionPlanDao paidSubscriptionPlanDao) {
        this.paidSubscriptionPlanDao = paidSubscriptionPlanDao;
    }

    public List<PaidSubscriptionPlan> getActivePlans(PaidSubscriptionTariffType tariffType) {
        return paidSubscriptionPlanDao.getActivePlans(tariffType);
    }

    public double getMinPrice() {
        return paidSubscriptionPlanDao.getMinPrice();
    }

    public PaidSubscriptionPlan getPlanById(int id) {
        return paidSubscriptionPlanDao.getById(id);
    }

    public Period getPlanPeriod(int id) {
        return paidSubscriptionPlanDao.getPlanPeriod(id);
    }
}
