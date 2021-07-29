package ru.gadjini.telegram.smart.bot.commons.domain;

import org.joda.time.Period;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

public class PaidSubscriptionPlan {

    public static final String ID = "id";

    public static final String PRICE = "price";

    //Удалить в ближайшем будущем
    public static final String CURRENCY = "currency";

    public static final String PERIOD = "period";

    public static final String TARIFF = "tariff";

    private int id;

    //In USD
    private double price;

    private Period period;

    private PaidSubscriptionTariffType tariff;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public void setTariff(PaidSubscriptionTariffType tariff) {
        this.tariff = tariff;
    }

    public PaidSubscriptionTariffType getTariff() {
        return tariff;
    }
}
