package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserBotDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserBotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean createIfNotExist(long userId, String botName) {
        return jdbcTemplate.update(
                "INSERT INTO user_bot(user_id, bot_name) VALUES(?, ?) ON CONFLICT(user_id, bot_name) DO NOTHING",
                ps -> {
                    ps.setLong(1, userId);
                    ps.setString(2, botName);
                }
        ) > 0;
    }
}
