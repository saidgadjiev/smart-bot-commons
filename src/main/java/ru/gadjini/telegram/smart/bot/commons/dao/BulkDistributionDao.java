package ru.gadjini.telegram.smart.bot.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.BulkDistribution;

import java.util.List;

@Repository
public class BulkDistributionDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public BulkDistributionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BulkDistribution> getDistributions(String botName, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM bulk_distribution WHERE bot_name = ? LIMIT " + limit,
                ps -> ps.setString(1, botName),
                (rs, rw) -> {
                    BulkDistribution bulkDistribution = new BulkDistribution();

                    bulkDistribution.setId(rs.getInt(BulkDistribution.ID));
                    bulkDistribution.setUserId(rs.getInt(BulkDistribution.USER_ID));
                    bulkDistribution.setMessageRu(rs.getString(BulkDistribution.MESSAGE_RU));
                    bulkDistribution.setMessageEn(rs.getString(BulkDistribution.MESSAGE_EN));
                    bulkDistribution.setMessageUz(rs.getString(BulkDistribution.MESSAGE_UZ));

                    return bulkDistribution;
                }
        );
    }

    public BulkDistribution getFirstDistribution(String botName) {
        List<BulkDistribution> distributions = getDistributions(botName, 1);

        return distributions.isEmpty() ? null : distributions.iterator().next();
    }

    public void delete(int id) {
        jdbcTemplate.update(
                "DELETE FROM bulk_distribution WHERE id = ?",
                ps -> ps.setInt(1, id)
        );
    }
}
