package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Map;

@Repository
@DB
public class DBPaidSubscriptionDao implements PaidSubscriptionDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DBPaidSubscriptionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(PaidSubscription paidSubscription) {
        jdbcTemplate.update(
                "INSERT INTO paid_subscription(user_id, bot_name, end_date, plan_id) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO NOTHING",
                ps -> setPaidSubscriptionCreateValues(ps, paidSubscription)
        );
        paidSubscription.setPurchaseDate(ZonedDateTime.now(TimeUtils.UTC));
    }

    @Override
    public PaidSubscription getByBotNameAndUserId(String botName, int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription WHERE bot_name = '" + botName + "' AND user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> rs.next() ? map(rs) : null
        );
    }

    @Override
    public void createOrRenew(PaidSubscription paidSubscription, Period period) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO paid_subscription(user_id, bot_name, end_date, plan_id) " +
                            "VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE " +
                            "SET purchase_date = now(), end_date = GREATEST(paid_subscription.end_date, now()) + ?, plan_id = ? " +
                            "RETURNING end_date", Statement.RETURN_GENERATED_KEYS);

                    setPaidSubscriptionCreateValues(ps, paidSubscription);
                    ps.setObject(5, JodaTimeUtils.toPgInterval(period));
                    ps.setInt(6, paidSubscription.getPlanId());

                    return ps;
                },
                generatedKeyHolder
        );

        Map<String, Object> keys = generatedKeyHolder.getKeys();
        Date endDate = (Date) keys.get(PaidSubscription.END_DATE);

        paidSubscription.setEndDate(endDate.toLocalDate());
        paidSubscription.setPurchaseDate(ZonedDateTime.now(TimeUtils.UTC));
    }

    private void setPaidSubscriptionCreateValues(PreparedStatement ps, PaidSubscription paidSubscription) throws SQLException {
        ps.setInt(1, paidSubscription.getUserId());
        ps.setString(2, paidSubscription.getBotName());
        ps.setDate(3, Date.valueOf(paidSubscription.getEndDate()));
        if (paidSubscription.getPlanId() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, paidSubscription.getPlanId());
        }
    }

    private PaidSubscription map(ResultSet rs) throws SQLException {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(rs.getInt(PaidSubscription.USER_ID));
        paidSubscription.setEndDate(rs.getDate(PaidSubscription.END_DATE).toLocalDate());
        paidSubscription.setBotName(rs.getString(PaidSubscription.BOT_NAME));
        int planId = rs.getInt(PaidSubscription.PLAN_ID);
        if (!rs.wasNull()) {
            paidSubscription.setPlanId(planId);
        }
        paidSubscription.setPurchaseDate(ZonedDateTime.of(rs.getTimestamp(PaidSubscription.PURCHASE_DATE).toLocalDateTime(), TimeUtils.UTC));

        return paidSubscription;
    }
}
