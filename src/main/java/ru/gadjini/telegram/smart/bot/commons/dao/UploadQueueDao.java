package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UploadQueueDao extends QueueDao {

    private JdbcTemplate jdbcTemplate;

    private Gson gson;

    private ObjectMapper objectMapper;

    @Autowired
    public UploadQueueDao(JdbcTemplate jdbcTemplate, @Qualifier("botapi") Gson gson, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.gson = gson;
        this.objectMapper = objectMapper;
    }

    public void create(UploadQueueItem queueItem) {
        jdbcTemplate.update(
                "INSERT INTO upload_queue (user_id, method, body, producer, progress, status, producer_id)\n" +
                        "    VALUES (?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, queueItem.getUserId());
                    ps.setString(2, queueItem.getMethod());
                    ps.setString(3, gson.toJson(queueItem.getBody()));
                    ps.setString(4, queueItem.getProducer());
                    try {
                        ps.setString(5, objectMapper.writeValueAsString(queueItem.getProgress()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    ps.setInt(6, queueItem.getStatus().getCode());
                    ps.setInt(7, queueItem.getProducerId());
                }
        );
    }

    public List<UploadQueueItem> getUploads(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                "SELECT * FROM upload_queue WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void deleteByProducerIds(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return;
        }
        jdbcTemplate.update(
                "DELETE FROM upload_queue WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")",
                ps -> {
                    ps.setString(1, producer);
                }
        );
    }

    public List<UploadQueueItem> poll(String producer, int limit) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE upload_queue SET " + QueueDao.POLL_UPDATE_LIST +
                        "WHERE id IN(SELECT id FROM upload_queue qu WHERE qu.status = 0 AND qu.next_run_at <= now() and qu.producer = ? " +
                        QueueDao.POLL_ORDER_BY + " LIMIT " + limit + ")\n" +
                        "RETURNING *\n" +
                        ")\n" +
                        "SELECT *\n" +
                        "FROM r",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public QueueDaoDelegate getQueueDaoDelegate() {
        return () -> UploadQueueItem.NAME;
    }

    private UploadQueueItem map(ResultSet rs) throws SQLException {
        UploadQueueItem item = new UploadQueueItem();
        item.setId(rs.getInt(UploadQueueItem.ID));

        item.setUserId(rs.getInt(UploadQueueItem.USER_ID));
        item.setProducer(rs.getString(UploadQueueItem.PRODUCER));
        item.setMethod(rs.getString(UploadQueueItem.METHOD));
        item.setBody(deserializeBody(item.getMethod(), rs.getString(UploadQueueItem.BODY)));

        Timestamp nextRunAt = rs.getTimestamp(UploadQueueItem.NEXT_RUN_AT);
        if (nextRunAt != null) {
            item.setNextRunAt(ZonedDateTime.of(nextRunAt.toLocalDateTime(), ZoneOffset.UTC));
        }
        String progress = rs.getString(UploadQueueItem.PROGRESS);
        if (StringUtils.isNotBlank(progress)) {
            try {
                item.setProgress(objectMapper.readValue(progress, Progress.class));
            } catch (JsonProcessingException e) {
                throw new SQLException(e);
            }
        }

        return item;
    }

    private Object deserializeBody(String method, String body) {
        switch (method) {
            case SendDocument.PATH:
                return gson.fromJson(body, SendDocument.class);
            case SendAudio.PATH:
                return gson.fromJson(body, SendAudio.class);
            case SendVideo.PATH:
                return gson.fromJson(body, SendVideo.class);
            case SendVoice.PATH:
                return gson.fromJson(body, SendVoice.class);
        }

        throw new IllegalArgumentException("Unsupported method " + method);
    }
}
