package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.PaidSubscriptionPlanDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.util.List;
import java.util.Objects;

@Service
public class PaidSubscriptionPlanService {

    private static final Integer TRIAL_PLAN_ID = null;

    private PaidSubscriptionPlanDao paidSubscriptionPlanDao;

    @Autowired
    public PaidSubscriptionPlanService(PaidSubscriptionPlanDao paidSubscriptionPlanDao) {
        this.paidSubscriptionPlanDao = paidSubscriptionPlanDao;
    }

    public PaidSubscriptionTariffType getTariff(Integer planId) {
        if (Objects.equals(planId, TRIAL_PLAN_ID)) {
            return PaidSubscriptionTariffType.FIXED;
        }

        return paidSubscriptionPlanDao.getTariff(planId);
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
