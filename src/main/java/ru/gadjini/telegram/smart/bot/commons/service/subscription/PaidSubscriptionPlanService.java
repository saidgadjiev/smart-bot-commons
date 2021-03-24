package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.PaidSubscriptionPlanDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;

@Service
public class PaidSubscriptionPlanService {

    private PaidSubscriptionPlanDao paidSubscriptionPlanDao;

    @Autowired
    public PaidSubscriptionPlanService(PaidSubscriptionPlanDao paidSubscriptionPlanDao) {
        this.paidSubscriptionPlanDao = paidSubscriptionPlanDao;
    }

    public PaidSubscriptionPlan getActivePlan() {
        return paidSubscriptionPlanDao.getActivePlan();
    }

    public PaidSubscriptionPlan getPlanById(int id) {
        return paidSubscriptionPlanDao.getById(id);
    }
}
