package ru.gadjini.telegram.smart.bot.commons.domain;

import org.joda.time.Period;

public class PaidSubscriptionPlan {

    public static final String ID = "id";

    public static final String PRICE = "price";

    public static final String CURRENCY = "currency";

    public static final String PERIOD = "period";

    private int id;

    private double price;

    private String currency;

    private Period period;

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
