package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.gadjini.telegram.smart.bot.commons.dao.DownloadingQueueDao;
import ru.gadjini.telegram.smart.bot.commons.dao.QueueDao;
import ru.gadjini.telegram.smart.bot.commons.property.QueueProperties;

@Configuration
public class RepositoryConfiguration {

    @Bean
    @Qualifier("downloading")
    public QueueDao downloadingQueueDao(JdbcTemplate jdbcTemplate, DownloadingQueueDao queueDaoDelegate, QueueProperties queueProperties) {
        return new QueueDao(jdbcTemplate, queueDaoDelegate, queueProperties);
    }
}
