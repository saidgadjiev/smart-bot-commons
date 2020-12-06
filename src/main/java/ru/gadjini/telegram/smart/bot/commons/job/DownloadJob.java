package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.dao.WorkQueueDao;
import ru.gadjini.telegram.smart.bot.commons.domain.DownloadQueueItem;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodControlException;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.property.FileManagerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.TempFileService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.file.FileDownloader;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.DownloadQueueService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DownloadJob extends WorkQueueJobPusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadJob.class);

    private static final String TAG = "down";

    private DownloadQueueService downloadingQueueService;

    private FileDownloader fileDownloader;

    private TempFileService tempFileService;

    private FileManagerProperties fileManagerProperties;

    private WorkQueueDao workQueueDao;

    private SmartExecutorService downloadTasksExecutor;

    private final List<DownloadQueueItem> currentDownloads = new ArrayList<>();

    private MessageService messageService;

    private UserService userService;

    @Value("${disable.jobs:false}")
    private boolean disableJobs;

    @Value("${enable.jobs.logging:false}")
    private boolean enableJobsLogging;

    @Autowired
    public DownloadJob(DownloadQueueService downloadingQueueService, FileDownloader fileDownloader,
                       TempFileService tempFileService, FileManagerProperties fileManagerProperties,
                       WorkQueueDao workQueueDao, @Qualifier("messageLimits") MessageService messageService, UserService userService) {
        this.downloadingQueueService = downloadingQueueService;
        this.fileDownloader = fileDownloader;
        this.tempFileService = tempFileService;
        this.fileManagerProperties = fileManagerProperties;
        this.workQueueDao = workQueueDao;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Autowired
    public void setDownloadTasksExecutor(@Qualifier("downloadTasksExecutor") SmartExecutorService downloadTasksExecutor) {
        this.downloadTasksExecutor = downloadTasksExecutor;
    }

    @PostConstruct
    public final void init() {
        LOGGER.debug("Disable jobs {}", disableJobs);
        LOGGER.debug("Enable jobs logging {}", enableJobsLogging);
        try {
            downloadingQueueService.resetProcessing();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        doDownloads();
    }

    public void rejectTask(SmartExecutorService.Job job) {
        downloadingQueueService.setWaitingAndDecrementAttempts(job.getId());
        LOGGER.debug("Rejected({})", job.getId());
    }

    @Scheduled(fixedDelay = 1000)
    public void doDownloads() {
        super.push();
    }

    @Override
    public SmartExecutorService getExecutor() {
        return downloadTasksExecutor;
    }

    @Override
    public boolean enableJobsLogging() {
        return enableJobsLogging;
    }

    @Override
    public boolean disableJobs() {
        return disableJobs;
    }

    @Override
    public Class<?> getLoggerClass() {
        return getClass();
    }

    @Override
    public List<QueueItem> getTasks(SmartExecutorService.JobWeight weight, int limit) {
        return (List<QueueItem>) (Object) downloadingQueueService.poll(workQueueDao.getQueueName(), weight, limit);
    }

    @Override
    public SmartExecutorService.Job createJob(QueueItem item) {
        return new DownloadTask((DownloadQueueItem) item);
    }

    public void cancelDownloads(String producer, int producerId) {
        cancelDownloads(producer, Set.of(producerId));
    }

    public void cancelDownloads(String producer, Set<Integer> producerIds) {
        deleteDownloads(producer, producerIds);
    }

    public void deleteDownloads(String producer, Set<Integer> producerIds) {
        List<DownloadQueueItem> deleted = downloadingQueueService.deleteByProducerIdsWithReturning(producer, producerIds);
        downloadTasksExecutor.cancel(deleted.stream().map(DownloadQueueItem::getId).collect(Collectors.toList()), true);
        releaseResources(deleted);
    }

    public void cancelDownloads() {
        downloadTasksExecutor.cancel(currentDownloads.stream().map(DownloadQueueItem::getId).collect(Collectors.toList()), false);
    }

    public final void shutdown() {
        downloadTasksExecutor.shutdown();
    }

    private void releaseResources(List<DownloadQueueItem> downloadQueueItems) {
        for (DownloadQueueItem downloadQueueItem : downloadQueueItems) {
            new SmartTempFile(new File(downloadQueueItem.getFilePath()), downloadQueueItem.isDeleteParentDir()).smartDelete();
        }
    }

    private class DownloadTask implements SmartExecutorService.Job {

        private DownloadQueueItem downloadingQueueItem;

        private volatile Supplier<Boolean> checker;

        private volatile boolean canceledByUser;

        private volatile SmartTempFile tempFile;

        private DownloadTask(DownloadQueueItem downloadingQueueItem) {
            this.downloadingQueueItem = downloadingQueueItem;
        }

        @Override
        public void execute() {
            currentDownloads.add(downloadingQueueItem);
            try {
                if (downloadingQueueItem.getFile().getFormat().isDownloadable()) {
                    doDownloadFile(downloadingQueueItem);
                } else {
                    downloadingQueueService.setCompleted(downloadingQueueItem.getId());
                }
            } finally {
                currentDownloads.remove(downloadingQueueItem);
            }
        }

        @Override
        public int getId() {
            return downloadingQueueItem.getId();
        }

        @Override
        public SmartExecutorService.JobWeight getWeight() {
            return SmartExecutorService.JobWeight.HEAVY;
        }

        @Override
        public long getChatId() {
            return downloadingQueueItem.getUserId();
        }

        @Override
        public void setCancelChecker(Supplier<Boolean> checker) {
            this.checker = checker;
        }

        @Override
        public Supplier<Boolean> getCancelChecker() {
            return checker;
        }

        @Override
        public void setCanceledByUser(boolean canceledByUser) {
            this.canceledByUser = canceledByUser;
        }

        @Override
        public boolean isCanceledByUser() {
            return canceledByUser;
        }

        @Override
        public void cancel() {
            fileDownloader.cancelDownloading(downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getFile().getSize());
            if (canceledByUser) {
                downloadingQueueService.deleteById(downloadingQueueItem.getId());
                LOGGER.debug("Canceled downloading({}, {}, {})", downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getProducer(), downloadingQueueItem.getProducerId());
            }
            if (tempFile != null) {
                tempFile.smartDelete();
            }
        }

        private void doDownloadFile(DownloadQueueItem downloadingQueueItem) {
            if (StringUtils.isBlank(downloadingQueueItem.getFilePath())) {
                tempFile = tempFileService.createTempFile(downloadingQueueItem.getUserId(), downloadingQueueItem.getFile().getFileId(), TAG, downloadingQueueItem.getFile().getFormat().getExt());
            } else {
                tempFile = new SmartTempFile(new File(downloadingQueueItem.getFilePath()));
            }
            try {
                fileDownloader.downloadFileByFileId(downloadingQueueItem.getFile().getFileId(), downloadingQueueItem.getFile().getSize(), downloadingQueueItem.getProgress(), tempFile);
                downloadingQueueService.setCompleted(downloadingQueueItem.getId(), tempFile.getAbsolutePath());
            } catch (Exception e) {
                tempFile.smartDelete();

                if (checker == null || !checker.get()) {
                    if (e instanceof FloodControlException) {
                        floodControlException(downloadingQueueItem, (FloodControlException) e);
                    } else if (e instanceof FloodWaitException) {
                        floodWaitException(downloadingQueueItem, (FloodWaitException) e);
                    } else if (FileDownloader.isNoneCriticalDownloadingException(e)) {
                        noneCriticalException(downloadingQueueItem, e);
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        downloadingQueueService.setExceptionStatus(downloadingQueueItem.getId(), e);
                        messageService.sendErrorMessage(downloadingQueueItem.getUserId(), userService.getLocaleOrDefault(downloadingQueueItem.getUserId()));
                    }
                }
            }
        }

        private void noneCriticalException(DownloadQueueItem downloadingQueueItem, Throwable e) {
            downloadingQueueService.setWaiting(downloadingQueueItem.getId(), fileManagerProperties.getSleepTimeBeforeDownloadAttempt(), e);
        }

        private void floodControlException(DownloadQueueItem downloadingQueueItem, FloodControlException e) {
            downloadingQueueService.setWaiting(downloadingQueueItem.getId(), e.getSleepTime(), e);
        }

        private void floodWaitException(DownloadQueueItem downloadingQueueItem, FloodWaitException e) {
            downloadingQueueService.setWaiting(downloadingQueueItem.getId(), e.getSleepTime(), e);
        }
    }
}
