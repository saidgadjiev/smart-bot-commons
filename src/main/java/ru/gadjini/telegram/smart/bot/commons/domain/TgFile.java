package ru.gadjini.telegram.smart.bot.commons.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import ru.gadjini.telegram.smart.bot.commons.service.format.Format;

import java.sql.SQLException;

public class TgFile {

    public static final String TYPE = "tg_file";

    public static final String FILE_ID = "file_id";

    public static final String FILE_NAME = "file_name";

    public static final String MIME_TYPE = "mime_type";

    public static final String SIZE = "size";

    public static final String THUMB = "thumb";

    public static final String FORMAT = "format";

    @JsonProperty(FILE_ID)
    private String fileId;

    @JsonProperty(FILE_NAME)
    private String fileName;

    @JsonProperty(MIME_TYPE)
    private String mimeType;

    @JsonProperty(FORMAT)
    private Format format;

    @JsonProperty(SIZE)
    private long size;

    @JsonProperty(THUMB)
    private String thumb;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String sql() {
        StringBuilder sql = new StringBuilder("(\"");

        sql.append(fileId).append("\",");
        if (StringUtils.isNotBlank(mimeType)) {
            sql.append("\"").append(mimeType).append("\",");
        } else {
            sql.append(",");
        }
        if (StringUtils.isNotBlank(fileName)) {
            sql.append("\"").append(fileName).append("\",");
        } else {
            sql.append(",");
        }
        sql.append(size).append(",");
        if (StringUtils.isNotBlank(thumb)) {
            sql.append("\"").append(thumb).append("\",");
        } else {
            sql.append(",");
        }
        if (format != null) {
            sql.append("\"").append(format.name()).append("\"");
        }

        sql.append(")");

        return sql.toString();
    }

    public PGobject sqlObject() {
        PGobject pGobject = new PGobject();
        pGobject.setType(TYPE);
        try {
            pGobject.setValue(sql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pGobject;
    }
}
