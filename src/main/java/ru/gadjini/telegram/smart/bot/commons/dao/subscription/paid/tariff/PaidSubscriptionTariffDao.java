package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid.tariff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionTariff;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PaidSubscriptionTariffDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PaidSubscriptionTariffDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PaidSubscriptionTariff> getTariffs() {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription_tariff WHERE active = true ORDER BY sort",
                (rs, rw) -> map(rs)
        );
    }

    private PaidSubscriptionTariff map(ResultSet rs) throws SQLException {
        PaidSubscriptionTariff tariff = new PaidSubscriptionTariff();
        tariff.setTariffType(PaidSubscriptionTariffType.valueOf(rs.getString(PaidSubscriptionTariff.TARIFF_TYPE).toUpperCase()));

        return tariff;
    }
}
