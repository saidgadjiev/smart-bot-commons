package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.model.Progress;

public class DownloadingQueueItem extends QueueItem {

    public static final String NAME = "downloading_queue";

    public static final String FILE = "file";

    public static final String PRODUCER = "producer";

    public static final String PRODUCER_ID = "producer_id";

    public static final String PROGRESS = "progress";

    public static final String FILE_PATH = "file_path";

    public static final String DELETE_PARENT_DIR = "delete_parent_dir";

    private TgFile file;

    private String producer;

    private Progress progress;

    private int producerId;

    private String filePath;

    private boolean deleteParentDir;

    public TgFile getFile() {
        return file;
    }

    public void setFile(TgFile file) {
        this.file = file;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDeleteParentDir() {
        return deleteParentDir;
    }

    public void setDeleteParentDir(boolean deleteParentDir) {
        this.deleteParentDir = deleteParentDir;
    }

    public int getProducerId() {
        return producerId;
    }

    public void setProducerId(int producerId) {
        this.producerId = producerId;
    }
}
