package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@DB
public class DBPaidSubscriptionDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DBPaidSubscriptionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(PaidSubscription paidSubscription) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(PaidSubscription.TABLE)
                .execute(sqlParameterSource(paidSubscription));
    }

    public PaidSubscription getPaidSubscription(String botName, int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription WHERE bot_name = '" + botName + "' AND user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> rs.next() ? map(rs) : null
        );
    }

    private SqlParameterSource sqlParameterSource(PaidSubscription paidSubscription) {
        return new MapSqlParameterSource()
                .addValue(PaidSubscription.USER_ID, paidSubscription.getUserId())
                .addValue(PaidSubscription.END_DATE, Date.valueOf(paidSubscription.getEndDate()))
                .addValue(PaidSubscription.PLAN_ID, paidSubscription.getPlanId());
    }

    private PaidSubscription map(ResultSet rs) throws SQLException {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(rs.getInt(PaidSubscription.USER_ID));
        paidSubscription.setEndDate(rs.getDate(PaidSubscription.END_DATE).toLocalDate());

        return paidSubscription;
    }
}
