package ru.gadjini.telegram.smart.bot.commons.dao.subscription;

import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscriptionPlan;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class PaidSubscriptionPlanDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PaidSubscriptionPlanDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PaidSubscriptionPlan getActivePlan() {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription_plan WHERE active = true",
                rs -> rs.next() ? map(rs) : null
        );
    }

    public PaidSubscriptionPlan getById(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription_plan WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? map(rs) : null
        );
    }

    private PaidSubscriptionPlan map(ResultSet rs) throws SQLException {
        PaidSubscriptionPlan paidSubscriptionPlan = new PaidSubscriptionPlan();
        paidSubscriptionPlan.setId(rs.getInt(PaidSubscriptionPlan.ID));
        paidSubscriptionPlan.setCurrency(rs.getString(PaidSubscriptionPlan.CURRENCY));
        paidSubscriptionPlan.setPrice(rs.getDouble(PaidSubscriptionPlan.PRICE));
        paidSubscriptionPlan.setPeriod(JodaTimeUtils.toPeriod((PGInterval) rs.getObject(PaidSubscriptionPlan.PERIOD)));

        return paidSubscriptionPlan;
    }
}
