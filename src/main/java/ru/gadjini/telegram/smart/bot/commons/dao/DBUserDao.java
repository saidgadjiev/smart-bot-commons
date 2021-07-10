package ru.gadjini.telegram.smart.bot.commons.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;

import java.sql.Statement;
import java.sql.Types;

@Repository
@DB
public class DBUserDao implements UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DBUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int updateActivity(long userId, String userName) {
        return jdbcTemplate.update(
                "UPDATE tg_user SET last_activity_at = now(), username = ? WHERE user_id = ?",
                ps -> {
                    if (StringUtils.isBlank(userName)) {
                        ps.setNull(1, Types.NULL);
                    } else {
                        ps.setString(1, userName);
                    }
                    ps.setLong(2, userId);
                }
        );
    }

    @Override
    public CreateOrUpdateResult.State createOrUpdate(TgUser user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    var ps = connection.prepareStatement(
                            "INSERT INTO tg_user(user_id, username, locale, original_locale, start_parameter) VALUES (?, ?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET " +
                                    "last_activity_at = now(), username = excluded.username, original_locale = excluded.original_locale, blocked = false " +
                                    "RETURNING CASE WHEN XMAX::text::int > 0 THEN 'updated' ELSE 'inserted' END AS state",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setLong(1, user.getUserId());
                    if (StringUtils.isBlank(user.getUsername())) {
                        ps.setNull(2, Types.NULL);
                    } else {
                        ps.setString(2, user.getUsername());
                    }
                    ps.setString(3, user.getLanguageCode());
                    if (StringUtils.isBlank(user.getOriginalLocale())) {
                        ps.setNull(4, Types.NULL);
                    } else {
                        ps.setString(4, user.getOriginalLocale());
                    }
                    if (StringUtils.isNotBlank(user.getStartParameter())) {
                        ps.setString(5, user.getStartParameter());
                    } else {
                        ps.setNull(5, Types.VARCHAR);
                    }

                    return ps;
                },
                keyHolder
        );

        return CreateOrUpdateResult.State.fromDesc((String) keyHolder.getKeys().get("state"));
    }

    @Override
    public void updateLocale(long userId, String language) {
        jdbcTemplate.update(
                "UPDATE tg_user SET locale = ? WHERE user_id = ?",
                ps -> {
                    ps.setString(1, language);
                    ps.setLong(2, userId);
                }
        );
    }

    @Override
    public void blockUser(long userId) {
        jdbcTemplate.update(
                "UPDATE tg_user SET blocked = true WHERE user_id = " + userId
        );
    }

    @Override
    public String getLocale(long userId) {
        return jdbcTemplate.query(
                "SELECT locale FROM tg_user WHERE user_id = ?",
                ps -> ps.setLong(1, userId),
                rs -> {
                    if (rs.next()) {
                        return rs.getString(TgUser.LOCALE);
                    }

                    return null;
                }
        );
    }

    @Override
    public Long countActiveUsers(int intervalInDays) {
        return jdbcTemplate.query(
                "SELECT count(user_id) as cnt FROM tg_user WHERE last_activity_at::date != created_at::date " +
                        "AND last_activity_at::date = current_date - interval '" + intervalInDays + " days'",
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }
}
