package ru.gadjini.telegram.smart.bot.commons.dao.subscription;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class PaidSubscriptionPlanDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PaidSubscriptionPlanDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PaidSubscriptionPlan> getActivePlans(PaidSubscriptionTariffType tariffType) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription_plan WHERE active = true and tariff = ? ORDER BY price",
                ps -> {
                    ps.setString(1, tariffType.name().toLowerCase());
                },
                (rs, rw) -> map(rs)
        );
    }

    public Double getMinPrice() {
        return jdbcTemplate.query(
                "SELECT MIN(price) as mm FROM paid_subscription_plan WHERE active = true",
                rs -> rs.next() ? rs.getDouble("mm") : 1
        );
    }

    public PaidSubscriptionPlan getById(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription_plan WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? map(rs) : null
        );
    }

    public Period getPlanPeriod(int id) {
        return jdbcTemplate.query(
                "SELECT period FROM paid_subscription_plan WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? JodaTimeUtils.toPeriod((PGInterval) rs.getObject(PaidSubscriptionPlan.PERIOD)) : null
        );
    }

    private PaidSubscriptionPlan map(ResultSet rs) throws SQLException {
        PaidSubscriptionPlan paidSubscriptionPlan = new PaidSubscriptionPlan();
        paidSubscriptionPlan.setId(rs.getInt(PaidSubscriptionPlan.ID));
        paidSubscriptionPlan.setPrice(rs.getDouble(PaidSubscriptionPlan.PRICE));
        paidSubscriptionPlan.setPeriod(JodaTimeUtils.toPeriod((PGInterval) rs.getObject(PaidSubscriptionPlan.PERIOD)));

        return paidSubscriptionPlan;
    }
}
