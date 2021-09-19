package ru.gadjini.telegram.smart.bot.commons.dao.tutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.annotation.DB;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@DB
public class DBTutorialDao implements TutorialDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DBTutorialDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Tutorial delete(int id) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM tutorial WHERE id = ? RETURNING command, bot_name",
                            PreparedStatement.RETURN_GENERATED_KEYS);

                    ps.setInt(1, id);

                    return ps;
                },
                generatedKeyHolder
        );

        if (generatedKeyHolder.getKeys() != null) {
            Tutorial tutorial = new Tutorial();
            tutorial.setId(id);
            tutorial.setCommand((String) generatedKeyHolder.getKeys().get(Tutorial.COMMAND));
            tutorial.setBotName((String) generatedKeyHolder.getKeys().get(Tutorial.BOT_NAME));

            return tutorial;
        }

        return null;
    }

    @Override
    public void create(Tutorial tutorial) {
        jdbcTemplate.update(
                "INSERT INTO tutorial(file_id, bot_name, cmd, description) VALUES (?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, tutorial.getFileId());
                    ps.setString(2, tutorial.getBotName());
                    ps.setString(3, tutorial.getCommand());
                    ps.setString(4, tutorial.getDescription());
                }
        );
    }

    @Override
    public List<Tutorial> getTutorials(String command, String botName) {
        return jdbcTemplate.query(
                "SELECT * FROM tutorial WHERE cmd = ? and bot_name = ? ORDER BY id",
                ps -> {
                    ps.setString(1, command);
                    ps.setString(2, botName);
                },
                (rs, num) -> map(rs)
        );
    }

    @Override
    public String getFileId(int id) {
        return jdbcTemplate.query(
                "SELECT file_id FROM tutorial WHERE id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.next() ? rs.getString(Tutorial.FILE_ID) : null
        );
    }

    @Override
    public List<Tutorial> getTutorials(String botName) {
        return jdbcTemplate.query(
                "SELECT * FROM tutorial WHERE bot_name = ? ORDER BY id",
                ps -> ps.setString(1, botName),
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
