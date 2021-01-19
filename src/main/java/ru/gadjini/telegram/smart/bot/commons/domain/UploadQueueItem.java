package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.model.Progress;

public class UploadQueueItem extends QueueItem {

    public static final String NAME = "upload_queue";

    public static final String PRODUCER_TABLE = "producer_table";

    public static final String PRODUCER = "producer";

    public static final String PRODUCER_ID = "producer_id";

    public static final String PROGRESS = "progress";

    public static final String METHOD = "method";

    public static final String BODY = "body";

    public static final String EXTRA = "extra";

    public static final String FILE_SIZE = "file_size";

    public static final String SUPPORTS_STREAMING = "supports_streaming";

    private String producerTable;

    private String producer;

    private Progress progress;

    private int producerId;

    private String method;

    private Object body;

    private Object extra;

    private long fileSize;

    private boolean supportsStreaming;

    public String getProducerTable() {
        return producerTable;
    }

    public void setProducerTable(String producerTable) {
        this.producerTable = producerTable;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getProducerId() {
        return producerId;
    }

    public void setProducerId(int producerId) {
        this.producerId = producerId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public boolean isSupportsStreaming() {
        return supportsStreaming;
    }

    public void setSupportsStreaming(boolean supportsStreaming) {
        this.supportsStreaming = supportsStreaming;
    }
}
