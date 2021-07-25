package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserSettingsDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserSettingsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createDefaultSettings(String botName, long userId) {
        jdbcTemplate.update(
                "INSERT INTO user_settings(bot_name, user_id, smart_file) VALUES (?, ?, true)",
                ps -> {
                    ps.setString(1, botName);
                    ps.setLong(2, userId);
                }
        );
    }

    public void smartFileFeature(String botName, long userId, boolean enable) {
        jdbcTemplate.update(
                "INSERT INTO user_settings(bot_name, user_id, smart_file) VALUES (?, ?, ?) " +
                        " ON CONFLICT(user_id, bot_name) DO UPDATE SET smart_file = excluded.smart_file",
                ps -> {
                    ps.setString(1, botName);
                    ps.setLong(2, userId);
                    ps.setBoolean(3, enable);
                }
        );
    }

    public Boolean getSmartFileFeatureEnabledOrDefault(String botName, long userId) {
        return jdbcTemplate.query(
                "WITH ins AS(INSERT INTO user_settings(bot_name, user_id, smart_file) VALUES (?, ?, true) " +
                        " ON CONFLICT(user_id, bot_name) DO NOTHING) SELECT smart_file FROM user_settings WHERE bot_name = ? AND user_id = ?",
                ps -> {
                    ps.setString(1, botName);
                    ps.setLong(2, userId);
                    ps.setString(3, botName);
                    ps.setLong(4, userId);
                },
                rs -> {
                    if (rs.next()) {
                        return rs.getBoolean("smart_file");
                    }

                    return true;
                }
        );
    }
}
