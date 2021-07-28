package ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.tariff.PaidSubscriptionTariffDao;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionTariff;

import java.util.List;

@Service
public class PaidSubscriptionTariffService {

    private PaidSubscriptionTariffDao tariffDao;

    @Autowired
    public PaidSubscriptionTariffService(PaidSubscriptionTariffDao tariffDao) {
        this.tariffDao = tariffDao;
    }

    public List<PaidSubscriptionTariff> getActiveTariffs() {
        return tariffDao.getTariffs();
    }
}
