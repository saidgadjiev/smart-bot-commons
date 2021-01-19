package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.UploadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.utils.JdbcUtils;

import java.sql.*;
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

    private MediaLimitProperties mediaLimitProperties;

    private WorkQueueDao workQueueDao;

    @Autowired
    public UploadQueueDao(JdbcTemplate jdbcTemplate, @Qualifier("botapi") Gson gson, ObjectMapper objectMapper,
                          MediaLimitProperties mediaLimitProperties, WorkQueueDao workQueueDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.mediaLimitProperties = mediaLimitProperties;
        this.workQueueDao = workQueueDao;
    }

    public void create(UploadQueueItem queueItem) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO upload_queue (user_id, method, body, producer_table, progress, status, producer_id, extra, file_size, producer)\n" +
                            "    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, queueItem.getUserId());
                    ps.setString(2, queueItem.getMethod());
                    ps.setString(3, gson.toJson(queueItem.getBody()));
                    ps.setString(4, queueItem.getProducerTable());
                    try {
                        ps.setString(5, objectMapper.writeValueAsString(queueItem.getProgress()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    ps.setInt(6, queueItem.getStatus().getCode());
                    ps.setInt(7, queueItem.getProducerId());
                    if (queueItem.getExtra() != null) {
                        ps.setString(8, gson.toJson(queueItem.getExtra()));
                    } else {
                        ps.setNull(8, Types.VARCHAR);
                    }
                    ps.setLong(9, queueItem.getFileSize());
                    ps.setString(10, queueItem.getProducer());

                    return ps;
                },
                keyHolder
        );

        int id = ((Number) keyHolder.getKeys().get(UploadQueueItem.ID)).intValue();
        queueItem.setId(id);
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

    public List<UploadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                "WITH del AS(DELETE FROM " + UploadQueueItem.NAME + " WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") RETURNING *) " +
                        "SELECT * FROM del",
                ps -> {
                    ps.setString(1, producer);
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE upload_queue SET " + QueueDao.POLL_UPDATE_LIST +
                        "WHERE id IN(SELECT id FROM upload_queue qu WHERE qu.status = 0 AND qu.next_run_at <= now() and qu.producer = ? " +
                        "AND file_size " + (jobWeight.equals(SmartExecutorService.JobWeight.LIGHT) ? "<=" : ">") + " ?\n" +
                        QueueDao.POLL_ORDER_BY + " LIMIT " + limit + ")\n" +
                        "RETURNING *\n" +
                        ")\n" +
                        "SELECT *\n" +
                        "FROM r",
                ps -> {
                    ps.setString(1, producer);
                    ps.setLong(2, mediaLimitProperties.getLightFileMaxWeight());
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public List<UploadQueueItem> deleteOrphan(String producer) {
        return jdbcTemplate.query(
                "WITH del AS(delete\n" +
                        "from upload_queue dq\n" +
                        "where producer = ?\n" +
                        "  and not exists(select 1 from " + producer + " uq where uq.id = dq.producer_id) RETURNING *) " +
                        "SELECT * FROM del",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void updateStatus(int id, QueueItem.Status newStatus, QueueItem.Status oldStatus) {
        jdbcTemplate.update(
                "UPDATE upload_queue SET status = ? WHERE id = ? AND status = ?",
                ps -> {
                    ps.setInt(1, newStatus.getCode());
                    ps.setInt(2, id);
                    ps.setInt(3, oldStatus.getCode());
                }
        );
    }

    public UploadQueueItem supportsStreaming(int id, boolean supportsStreaming) {
        return jdbcTemplate.query(
                "WITH upd AS (UPDATE upload_queue SET supports_streaming = ? WHERE id = ? AND status = ? RETURNING id, supports_streaming)\n" +
                        "SELECT * FROM upd",
                ps -> {
                    ps.setBoolean(1, supportsStreaming);
                    ps.setInt(2, id);
                    ps.setInt(3, QueueItem.Status.BLOCKED.getCode());
                },
                rs -> rs.next() ? map(rs) : null
        );
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public QueueDaoDelegate getQueueDaoDelegate() {
        return new QueueDaoDelegate() {
            @Override
            public String getQueueName() {
                return UploadQueueItem.NAME;
            }

            @Override
            public String getBaseAdditionalClause() {
                return "AND producer = '" + workQueueDao.getProducerName() + "'";
            }
        };
    }

    private UploadQueueItem map(ResultSet rs) throws SQLException {
        UploadQueueItem item = new UploadQueueItem();
        item.setId(rs.getInt(UploadQueueItem.ID));

        item.setUserId(rs.getInt(UploadQueueItem.USER_ID));
        item.setProducerTable(rs.getString(UploadQueueItem.PRODUCER_TABLE));
        item.setProducerId(rs.getInt(UploadQueueItem.PRODUCER_ID));
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
        String extra = rs.getString(UploadQueueItem.EXTRA);
        if (StringUtils.isNotBlank(extra)) {
            item.setExtra(gson.fromJson(extra, JsonElement.class));
        }
        Set<String> columns = JdbcUtils.getColumnNames(rs.getMetaData());
        if (columns.contains(UploadQueueItem.SUPPORTS_STREAMING)) {
            item.setSupportsStreaming(rs.getBoolean(UploadQueueItem.SUPPORTS_STREAMING));
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
