package ru.gadjini.telegram.smart.bot.commons.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadingQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.TgFile;
import ru.gadjini.telegram.smart.bot.commons.model.Progress;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DownloadingQueueDao extends QueueDao {

    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public DownloadingQueueDao(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, () -> DownloadingQueueItem.NAME);
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void create(DownloadingQueueItem queueItem) {
        jdbcTemplate.update(
                "INSERT INTO downloading_queue (user_id, file, producer, progress, status, file_path, delete_parent_dir)\n" +
                        "    VALUES (?, ?, ?, ?, ?)",
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
                }
        );
    }

    public List<DownloadingQueueItem> poll(String producer) {
        return jdbcTemplate.query(
                "WITH r AS (\n" +
                        "    UPDATE downloading_queue SET " + QueueDao.POLL_UPDATE_LIST +
                        "WHERE status = 0 AND next_run_at <= now() and producer = ? RETURNING *\n" +
                        ")\n" +
                        "SELECT *, (file).*\n" +
                        "FROM r",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void setCompleted(int id, String filePath) {
        jdbcTemplate.update(
                "UPDATE downloading_queue SET status = ?, completed_at = now(), file_path = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, QueueItem.Status.COMPLETED.getCode());
                    ps.setString(2, filePath);
                    ps.setInt(3, id);
                }
        );
    }

    public List<DownloadingQueueItem> getDownloads(Collection<String> filesIds, String producer) {
        return jdbcTemplate.query(
                "SELECT DISTINCT ON((file).file_id) *, (file).* FROM downloading_queue " +
                        "WHERE (file).file_id IN(" + filesIds.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")) + ") AND producer = ?",
                ps -> ps.setString(1, producer),
                (rs, rowNum) -> map(rs)
        );
    }

    public void deleteByIds(Collection<Integer> ids) {
        jdbcTemplate.update(
                "DELETE FROM downloading_queue WHERE id IN(" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")"
        );
    }

    public void deleteByFileId(String fileId, String producer) {
        jdbcTemplate.update(
                "DELETE FROM downloading_queue WHERE id = (SELECT id from downloading_queue where (file).file_id = ? AND producer = ?)",
                ps -> {
                    ps.setString(1, fileId);
                    ps.setString(2, producer);
                }
        );
    }

    public DownloadingQueueItem map(ResultSet rs) throws SQLException {
        DownloadingQueueItem item = new DownloadingQueueItem();
        item.setId(rs.getInt(DownloadingQueueItem.ID));

        TgFile tgFile = new TgFile();
        tgFile.setFileId(rs.getString(TgFile.FILE_ID));
        tgFile.setFileName(rs.getString(TgFile.FILE_NAME));
        tgFile.setMimeType(rs.getString(TgFile.MIME_TYPE));
        tgFile.setSize(rs.getLong(TgFile.SIZE));
        tgFile.setThumb(rs.getString(TgFile.THUMB));
        item.setFile(tgFile);

        item.setUserId(rs.getInt(DownloadingQueueItem.USER_ID));
        item.setProducer(rs.getString(DownloadingQueueItem.PRODUCER));
        item.setFilePath(rs.getString(DownloadingQueueItem.FILE_PATH));
        item.setDeleteParentDir(rs.getBoolean(DownloadingQueueItem.DELETE_PARENT_DIR));

        Timestamp nextRunAt = rs.getTimestamp(DownloadingQueueItem.NEXT_RUN_AT);
        if (nextRunAt != null) {
            item.setNextRunAt(ZonedDateTime.from(nextRunAt.toLocalDateTime()));
        }
        String progress = rs.getString(DownloadingQueueItem.PROGRESS);
        if (StringUtils.isNotBlank(progress)) {
            try {
                item.setProgress(objectMapper.readValue(progress, Progress.class));
            } catch (JsonProcessingException e) {
                throw new SQLException(e);
            }
        }

        return item;
    }
}
