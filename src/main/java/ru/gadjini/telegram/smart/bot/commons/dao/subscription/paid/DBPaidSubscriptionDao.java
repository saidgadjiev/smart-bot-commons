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
    public PaidSubscription activateSubscriptionDay(long userId) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement ps = conn.prepareStatement("UPDATE paid_subscription " +
                            "SET end_at = now() + interval '1 days', " +
                            "subscription_interval = subscription_interval - interval '1 days' " +
                            "WHERE user_id = ? AND subscription_interval > interval '0 days' RETURNING end_at, subscription_interval", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, userId);

                    return ps;
                },
                generatedKeyHolder
        );

        if (generatedKeyHolder.getKeyList().isEmpty()) {
            return null;
        }

        Timestamp endDate = (Timestamp) generatedKeyHolder.getKeys().get(PaidSubscription.END_AT);

        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(userId);
        paidSubscription.setEndAt(ZonedDateTime.from(endDate.toLocalDateTime().atZone(TimeUtils.UTC)));

        PGInterval interval = (PGInterval) generatedKeyHolder.getKeys().get(PaidSubscription.SUBSCRIPTION_INTERVAL);
        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod(interval));

        return paidSubscription;
    }

    @Override
    public void create(PaidSubscription paidSubscription) {
        jdbcTemplate.update(
                "INSERT INTO paid_subscription(user_id, end_at, plan_id, subscription_interval) " +
                        "VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO NOTHING",
                ps -> setPaidSubscriptionCreateValues(ps, paidSubscription)
        );
        paidSubscription.setPurchasedAt(ZonedDateTime.now(TimeUtils.UTC));
    }

    @Override
    public PaidSubscription getByUserId(long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription WHERE user_id = ?",
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? map(rs) : null
        );
    }

    @Override
    public void createOrRenew(PaidSubscription paidSubscription, Period period) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO paid_subscription(user_id, end_at, plan_id, subscription_interval) " +
                            "VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE " +
                            "SET purchased_at = now(), end_at = GREATEST(paid_subscription.end_at, now()) + ?, plan_id = ?, " +
                            " subscription_interval = GREATEST(paid_subscription.subscription_interval, interval '0 days') + ? RETURNING end_at, subscription_interval", Statement.RETURN_GENERATED_KEYS);

                    setPaidSubscriptionCreateValues(ps, paidSubscription);
                    if (paidSubscription.getEndAt() == null) {
                        ps.setNull(5, Types.OTHER);
                    } else {
                        ps.setObject(5, JodaTimeUtils.toPgInterval(period));
                    }
                    ps.setInt(6, paidSubscription.getPlanId());
                    if (paidSubscription.getSubscriptionInterval() != null) {
                        ps.setObject(7, JodaTimeUtils.toPgIntervalDays(period));
                    } else {
                        ps.setNull(7, Types.OTHER);
                    }

                    return ps;
                },
                generatedKeyHolder
        );

        Map<String, Object> keys = generatedKeyHolder.getKeys();
        Timestamp endAt = (Timestamp) keys.get(PaidSubscription.END_AT);

        if (endAt != null) {
            paidSubscription.setEndAt(ZonedDateTime.from(endAt.toLocalDateTime().atZone(TimeUtils.UTC)));
        }
        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod((PGInterval) keys.get(PaidSubscription.SUBSCRIPTION_INTERVAL)));
        paidSubscription.setPurchasedAt(ZonedDateTime.now(TimeUtils.UTC));
    }

    @Override
    public int remove(long userId) {
        return jdbcTemplate.update(
                "DELETE FROM paid_subscription WHERE user_id = ?",
                ps -> ps.setLong(1, userId)
        );
    }

    @Override
    public void refresh(long userId) {

    }

    private void setPaidSubscriptionCreateValues(PreparedStatement ps, PaidSubscription paidSubscription) throws SQLException {
        ps.setLong(1, paidSubscription.getUserId());
        if (paidSubscription.getEndAt() != null) {
            ps.setTimestamp(2, Timestamp.valueOf(paidSubscription.getEndAt().toLocalDateTime()));
        } else {
            ps.setNull(2, Types.TIMESTAMP);
        }
        if (paidSubscription.getPlanId() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, paidSubscription.getPlanId());
        }
        if (paidSubscription.getSubscriptionInterval() == null) {
            ps.setNull(4, Types.OTHER);
        } else {
            ps.setObject(4, JodaTimeUtils.toPgIntervalDays(paidSubscription.getSubscriptionInterval()));
        }
    }

    private PaidSubscription map(ResultSet rs) throws SQLException {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(rs.getLong(PaidSubscription.USER_ID));

        Timestamp endAt = rs.getTimestamp(PaidSubscription.END_AT);
        if (endAt != null) {
            paidSubscription.setEndAt(ZonedDateTime.from(endAt.toLocalDateTime().atZone(TimeUtils.UTC)));
        }

        paidSubscription.setSubscriptionInterval(JodaTimeUtils.toPeriod((PGInterval) rs.getObject(PaidSubscription.SUBSCRIPTION_INTERVAL)));
        int planId = rs.getInt(PaidSubscription.PLAN_ID);
        if (!rs.wasNull()) {
            paidSubscription.setPlanId(planId);
        }
        paidSubscription.setPurchasedAt(ZonedDateTime.of(rs.getTimestamp(PaidSubscription.PURCHASED_AT).toLocalDateTime(), TimeUtils.UTC));

        return paidSubscription;
    }
}
