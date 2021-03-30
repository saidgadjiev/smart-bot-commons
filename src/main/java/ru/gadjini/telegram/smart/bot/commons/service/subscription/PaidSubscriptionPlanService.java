package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.PaidSubscriptionPlanDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;

import java.util.List;

@Service
public class PaidSubscriptionPlanService {

    private PaidSubscriptionPlanDao paidSubscriptionPlanDao;

    @Autowired
    public PaidSubscriptionPlanService(PaidSubscriptionPlanDao paidSubscriptionPlanDao) {
        this.paidSubscriptionPlanDao = paidSubscriptionPlanDao;
    }

    public List<PaidSubscriptionPlan> getActivePlans() {
        return paidSubscriptionPlanDao.getActivePlans();
    }

    public double getMinPrice() {
        return paidSubscriptionPlanDao.getMinPrice();
    }

    public PaidSubscriptionPlan getPlanById(int id) {
        return paidSubscriptionPlanDao.getById(id);
    }
}
