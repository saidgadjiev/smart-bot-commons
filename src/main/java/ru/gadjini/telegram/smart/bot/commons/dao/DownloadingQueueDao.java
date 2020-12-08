package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;
import ru.gadjini.telegram.smart.bot.commons.property.MediaLimitProperties;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloader;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class DownloadingQueueDao extends QueueDao {

    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;

    private MediaLimitProperties mediaLimitProperties;

    private Gson gson;

    @Autowired
    public DownloadingQueueDao(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, MediaLimitProperties mediaLimitProperties, Gson gson) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.mediaLimitProperties = mediaLimitProperties;
        this.gson = gson;
    }

    public void create(DownloadQueueItem queueItem) {
        jdbcTemplate.update(
                "INSERT INTO " + DownloadQueueItem.NAME + " (user_id, file, producer, progress, status, file_path, delete_parent_dir, producer_id, extra)\n" +
                        "    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, queueItem.getUserId());

                    ps.setObject(2, queueItem.getFile().sqlObject());

                    ps.setString(3, queueItem.getProducer());
                    try {
                        ps.setString(4, objectMapper.writeValueAsString(queueItem.getProgress()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    ps.setInt(5, queueItem.getStatus().getCode());
                    if (StringUtils.isNotBlank(queueItem.getFilePath())) {
                        ps.setString(6, queueItem.getFilePath());
                    } else {
                        ps.setNull(6, Types.VARCHAR);
                    }
                    ps.setBoolean(7, queueItem.isDeleteParentDir());
                    ps.setInt(8, queueItem.getProducerId());
                    if (queueItem.getExtra() == null) {
                        ps.setNull(9, Types.VARCHAR);
                    } else {
                        ps.setString(9, gson.toJson(queueItem.getExtra()));
                    }
                }
        );
    }

    public List<DownloadQueueItem> poll(String producer, SmartExecutorService.JobWeight jobWeight, int limit) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE " + DownloadQueueItem.NAME + " SET " + QueueDao.POLL_UPDATE_LIST +
                        "WHERE id IN(SELECT id FROM " + DownloadQueueItem.NAME + " qu WHERE qu.status = 0 AND qu.next_run_at <= now() and qu.producer = ? " +
                        "AND (file).size " + (jobWeight.equals(SmartExecutorService.JobWeight.LIGHT) ? "<=" : ">") + "  ?\n" +
                        QueueDao.POLL_ORDER_BY + " LIMIT " + limit + ")\n" +
                        "RETURNING *\n" +
                        ")\n" +
                        "SELECT *, (file).*\n" +
                        "FROM r",
                ps -> {
                    ps.setString(1, producer);
                    ps.setLong(2, mediaLimitProperties.getLightFileMaxWeight());
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public void setCompleted(int id, String filePath) {
        jdbcTemplate.update(
                "UPDATE " + DownloadQueueItem.NAME + " SET status = ?, completed_at = now(), file_path = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.COMPLETED.getCode());
                    ps.setString(2, filePath);
                    ps.setInt(3, id);
                }
        );
    }

    public List<DownloadQueueItem> getDownloads(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                "SELECT *, (file).* FROM " + getQueueName() + " WHERE producer = ? AND producer_id IN("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public List<DownloadQueueItem> deleteByProducerIdsWithReturning(String producer, Set<Integer> producerIds) {
        if (producerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                "WITH del AS (DELETE\n" +
                        "FROM " + DownloadQueueItem.NAME + "\n" +
                        "WHERE producer = ? AND producer_id IN ("
                        + producerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") RETURNING *)\n" +
                        "SELECT *, (file).* FROM del",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public long countWrongFileIdErrors() {
        return getJdbcTemplate().query(
                "SELECT COUNT(*) as cnt FROM " + getQueueName() + " WHERE status = ? AND exception like '%" + FileDownloader.FILE_ID_TEMPORARILY_UNAVAILABLE + "%'",
                ps -> ps.setInt(1, QueueItem.Status.EXCEPTION.getCode()),
                rs -> rs.next() ? rs.getLong("cnt") : 0
        );
    }

    public List<DownloadQueueItem> deleteOrphan(String producer) {
        return jdbcTemplate.query(
                "WITH del as (delete\n" +
                        "from downloading_queue dq\n" +
                        "where producer = ?\n" +
                        "  and not exists(select 1 from " + producer + " uq where uq.id = dq.producer_id) RETURNING *) " +
                        "SELECT *, (file).* FROM del",
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
        return () -> DownloadQueueItem.NAME;
    }

    public DownloadQueueItem map(ResultSet rs) throws SQLException {
        DownloadQueueItem item = new DownloadQueueItem();
        item.setId(rs.getInt(DownloadQueueItem.ID));

        TgFile tgFile = new TgFile();
        tgFile.setFileId(rs.getString(TgFile.FILE_ID));
        tgFile.setFileName(rs.getString(TgFile.FILE_NAME));
        tgFile.setMimeType(rs.getString(TgFile.MIME_TYPE));
        tgFile.setSize(rs.getLong(TgFile.SIZE));
        tgFile.setThumb(rs.getString(TgFile.THUMB));
        tgFile.setFormat(Format.valueOf(rs.getString(TgFile.FORMAT)));
        item.setFile(tgFile);

        item.setUserId(rs.getInt(DownloadQueueItem.USER_ID));
        item.setProducer(rs.getString(DownloadQueueItem.PRODUCER));
        item.setProducerId(rs.getInt(DownloadQueueItem.PRODUCER_ID));
        item.setFilePath(rs.getString(DownloadQueueItem.FILE_PATH));
        item.setDeleteParentDir(rs.getBoolean(DownloadQueueItem.DELETE_PARENT_DIR));

        Timestamp nextRunAt = rs.getTimestamp(DownloadQueueItem.NEXT_RUN_AT);
        if (nextRunAt != null) {
            item.setNextRunAt(ZonedDateTime.of(nextRunAt.toLocalDateTime(), ZoneOffset.UTC));
        }
        String progress = rs.getString(DownloadQueueItem.PROGRESS);
        if (StringUtils.isNotBlank(progress)) {
            try {
                item.setProgress(objectMapper.readValue(progress, Progress.class));
            } catch (JsonProcessingException e) {
                throw new SQLException(e);
            }
        }
        item.setExtra(gson.fromJson(rs.getString(DownloadQueueItem.EXTRA), JsonElement.class));

        return item;
    }
}
