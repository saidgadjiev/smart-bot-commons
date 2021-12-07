package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;

@Repository
@DB
public class DBBlackListDao implements BlackListDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DBBlackListDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Boolean isInBlackList(long userId) {
        return jdbcTemplate.query(
                "SELECT 1 FROM black_list WHERE user_id = ?",
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? true : false
        );
    }
}
