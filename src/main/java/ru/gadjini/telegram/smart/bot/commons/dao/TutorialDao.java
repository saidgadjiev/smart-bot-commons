package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TutorialDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TutorialDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tutorial> getTutorials(String command) {
        return jdbcTemplate.query(
                "SELECT * FROM tutorial WHERE cmd = ? ORDER BY id",
                ps -> ps.setString(1, command),
                (rs, num) -> map(rs)
        );
    }

    public String getFileId(int id) {
        return jdbcTemplate.query(
                "SELECT file_id FROM tutorial WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? rs.getString(Tutorial.FILE_ID) : null
        );
    }

    public List<Tutorial> getTutorials() {
        return jdbcTemplate.query(
                "SELECT * FROM tutorial ORDER BY id",
                (rs, num) -> map(rs)
        );
    }

    private Tutorial map(ResultSet rs) throws SQLException {
        Tutorial tutorial = new Tutorial();

        tutorial.setId(rs.getInt(Tutorial.ID));
        tutorial.setCommand(rs.getString(Tutorial.COMMAND));
        tutorial.setDescription(rs.getString(Tutorial.DESCRIPTION));

        return tutorial;
    }
}
