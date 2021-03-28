package ru.gadjini.telegram.smart.bot.commons.dao.subscription.paid;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;
import ru.gadjini.telegram.smart.bot.commons.utils.JodaTimeUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
                "INSERT INTO paid_subscription(user_id, bot_name, end_date, plan_id) VALUES (?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, paidSubscription.getUserId());
                    ps.setString(2, paidSubscription.getBotName());
                    ps.setDate(3, Date.valueOf(paidSubscription.getEndDate()));
                    if (paidSubscription.getPlanId() == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, paidSubscription.getPlanId());
                    }
                }
        );
        paidSubscription.setPurchaseDate(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public PaidSubscription getPaidSubscription(String botName, int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM paid_subscription WHERE bot_name = '" + botName + "' AND user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> rs.next() ? map(rs) : null
        );
    }

    @Override
    public LocalDate updateEndDate(String botName, int userId, int planId, Period period) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("UPDATE paid_subscription " +
                            "SET purchase_date = now(), end_date = GREATEST(end_date, now()) + ?, plan_id = ? " +
                            "WHERE user_id = ? RETURNING end_date", Statement.RETURN_GENERATED_KEYS);

                    ps.setObject(1, JodaTimeUtils.toPgInterval(period));
                    ps.setInt(2, planId);
                    ps.setInt(3, userId);

                    return ps;
                },
                generatedKeyHolder
        );

        Map<String, Object> keys = generatedKeyHolder.getKeys();
        Date endDate = (Date) keys.get(PaidSubscription.END_DATE);

        return endDate.toLocalDate();
    }

    private PaidSubscription map(ResultSet rs) throws SQLException {
        PaidSubscription paidSubscription = new PaidSubscription();
        paidSubscription.setUserId(rs.getInt(PaidSubscription.USER_ID));
        paidSubscription.setEndDate(rs.getDate(PaidSubscription.END_DATE).toLocalDate());
        paidSubscription.setBotName(rs.getString(PaidSubscription.BOT_NAME));
        paidSubscription.setPurchaseDate(rs.getTimestamp(PaidSubscription.PURCHASE_DATE).toLocalDateTime());

        return paidSubscription;
    }
}
