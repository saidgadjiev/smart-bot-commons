package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import org.postgresql.util.PGInterval;
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
    public PaidSubscription activateSubscriptionDay(String botName, long userId) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement("UPDATE paid_subscription " +
                            "SET end_date = current_date, " +
                            "subscription_interval = subscription_interval - interval '1 days' " +
                            "WHERE user_id = ? AND bot_name = ? RETURNING end_date, subscription_interval", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, userId);
                    ps.setString(2, botName);

                    return ps;
                },
                generatedKeyHolder
        );

        Date endDate = (Date) generatedKeyHolder.getKeys().get("end_date");

        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setBotName(botName);
        paidSubscription.setEndDate(endDate.toLocalDate());

        PGInterval interval = (PGInterval) generatedKeyHolder.getKeys().get(PaidSubscription.SUBSCRIPTION_INTERVAL);
        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod(interval));

        return paidSubscription;
    }

    @Override
    public void create(PaidSubscription paidSubscription) {
        jdbcTemplate.update(
                "INSERT INTO paid_subscription(user_id, bot_name, end_date, plan_id, subscription_interval) VALUES (?, ?, ?, ?, ?) ON CONFLICT(user_id) DO NOTHING",
                ps -> setPaidSubscriptionCreateValues(ps, paidSubscription)
        );
        paidSubscription.setPurchaseDate(ZonedDateTime.now(TimeUtils.UTC));
    }

    @Override
    public PaidSubscription getByBotNameAndUserId(String botName, long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription WHERE bot_name = '" + botName + "' AND user_id = ?",
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? map(rs) : null
        );
    }

    @Override
    public void createOrRenew(PaidSubscription paidSubscription, Period period) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO paid_subscription(user_id, bot_name, end_date, plan_id, subscription_interval) " +
                            "VALUES (?, ?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE " +
                            "SET purchase_date = now(), end_date = GREATEST(paid_subscription.end_date, now()) + ?, plan_id = ?, " +
                            " subscription_interval = paid_subscription.subscription_interval + ? RETURNING end_date, subscription_interval", Statement.RETURN_GENERATED_KEYS);

                    setPaidSubscriptionCreateValues(ps, paidSubscription);
                    if (paidSubscription.getEndDate() == null) {
                        ps.setNull(6, Types.OTHER);
                    } else {
                        ps.setObject(6, JodaTimeUtils.toPgInterval(period));
                    }
                    ps.setInt(7, paidSubscription.getPlanId());
                    if (paidSubscription.getSubscriptionInterval() != null) {
                        ps.setObject(8, JodaTimeUtils.toPgIntervalDays(period));
                    } else {
                        ps.setNull(8, Types.OTHER);
                    }

                    return ps;
                },
                generatedKeyHolder
        );

        Map<String, Object> keys = generatedKeyHolder.getKeys();
        Date endDate = (Date) keys.get(PaidSubscription.END_DATE);

        if (endDate != null) {
            paidSubscription.setEndDate(endDate.toLocalDate());
        }
        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod((PGInterval) keys.get(PaidSubscription.SUBSCRIPTION_INTERVAL)));
        paidSubscription.setPurchaseDate(ZonedDateTime.now(TimeUtils.UTC));
    }

    @Override
    public int remove(String botName, long userId) {
        return jdbcTemplate.update(
                "DELETE FROM paid_subscription WHERE bot_name = '" + botName + "' AND user_id = ?",
                ps -> ps.setLong(1, userId)
        );
    }

    @Override
    public void refresh(String botName, long userId) {

    }

    private void setPaidSubscriptionCreateValues(PreparedStatement ps, PaidSubscription paidSubscription) throws SQLException {
        ps.setLong(1, paidSubscription.getUserId());
        ps.setString(2, paidSubscription.getBotName());
        if (paidSubscription.getEndDate() != null) {
            ps.setDate(3, Date.valueOf(paidSubscription.getEndDate()));
        } else {
            ps.setNull(3, Types.DATE);
        }
        if (paidSubscription.getPlanId() == null) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, paidSubscription.getPlanId());
        }
        if (paidSubscription.getSubscriptionInterval() == null) {
            ps.setNull(5, Types.OTHER);
        } else {
            ps.setObject(5, JodaTimeUtils.toPgIntervalDays(paidSubscription.getSubscriptionInterval()));
        }
    }

    private PaidSubscription map(ResultSet rs) throws SQLException {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(rs.getInt(PaidSubscription.USER_ID));

        Date endDate = rs.getDate(PaidSubscription.END_DATE);
        if (endDate != null) {
            paidSubscription.setEndDate(endDate.toLocalDate());
        }

        paidSubscription.setBotName(rs.getString(PaidSubscription.BOT_NAME));
        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod((PGInterval) rs.getObject(PaidSubscription.SUBSCRIPTION_INTERVAL)));
        int planId = rs.getInt(PaidSubscription.PLAN_ID);
        if (!rs.wasNull()) {
            paidSubscription.setPlanId(planId);
        }
        paidSubscription.setPurchaseDate(ZonedDateTime.of(rs.getTimestamp(PaidSubscription.PURCHASE_DATE).toLocalDateTime(), TimeUtils.UTC));

        return paidSubscription;
    }
}
