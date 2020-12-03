package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.gadjini.telegram.smart.bot.commons.job.DownloadJob;
import ru.gadjini.telegram.smart.bot.commons.job.UploadJob;
import ru.gadjini.telegram.smart.bot.commons.job.WorkQueueJob;
import ru.gadjini.telegram.smart.bot.commons.property.FloodControlProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class SchedulerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfiguration.class);

    private WorkQueueJob conversionJob;

    private DownloadJob downloadingJob;

    private UploadJob uploadJob;

    @Value("${light.threads:2}")
    private int lightThreads;

    @Value("${heavy.threads:4}")
    private int heavyThreads;

    @PostConstruct
    public void init() {
        LOGGER.debug("Light threads({})", lightThreads);
        LOGGER.debug("Heavy threads({})", heavyThreads);
    }

    @Autowired
    public void setConversionJob(WorkQueueJob conversionJob) {
        this.conversionJob = conversionJob;
    }

    @Autowired
    public void setDownloadingJob(DownloadJob downloadingJob) {
        this.downloadingJob = downloadingJob;
    }

    @Autowired
    public void setUploadJob(UploadJob uploadJob) {
        this.uploadJob = uploadJob;
    }

    @Bean
    @Qualifier("queueTaskExecutor")
    public SmartExecutorService conversionTaskExecutor(UserService userService,
                                                       @Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor lightTaskExecutor = new ThreadPoolExecutor(lightThreads, lightThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(heavyThreads, heavyThreads, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Conversion light thread pool({})", lightTaskExecutor.getCorePoolSize());
        LOGGER.debug("Conversion heavy thread pool({})", heavyTaskExecutor.getCorePoolSize());

        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, lightTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> conversionJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> conversionJob.rejectTask(job));

        return executorService;
    }

    @Bean
    public TaskScheduler jobsThreadPoolTaskScheduler(UserService userService) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskScheduler.setThreadNamePrefix("JobsThreadPoolTaskScheduler");
        threadPoolTaskScheduler.setErrorHandler(throwable -> {
            LOGGER.error(throwable.getMessage(), throwable);
            userService.handleBotBlockedByUser(throwable);
        });

        LOGGER.debug("Jobs thread pool scheduler initialized with pool size: {}", threadPoolTaskScheduler.getPoolSize());

        return threadPoolTaskScheduler;
    }

    @Bean
    @Qualifier("downloadTasksExecutor")
    public SmartExecutorService downloadTasksExecutor(FloodControlProperties floodControlProperties, UserService userService,
                                                      @Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Download threads initialized with pool size: {}", floodControlProperties.getSleepOnDownloadingFloodWait());
        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, heavyTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> downloadingJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> downloadingJob.rejectTask(job));

        return executorService;
    }

    @Bean
    @Qualifier("uploadTasksExecutor")
    public SmartExecutorService uploadTasksExecutor(UserService userService,
                                                    @Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService) {
        SmartExecutorService executorService = new SmartExecutorService(messageService, localisationService, userService);
        ThreadPoolExecutor heavyTaskExecutor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, new SynchronousQueue<>()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                executorService.complete(r);
            }
        };

        LOGGER.debug("Download threads initialized with pool size: {}", heavyTaskExecutor.getCorePoolSize());
        executorService.setExecutors(Map.of(SmartExecutorService.JobWeight.LIGHT, heavyTaskExecutor, SmartExecutorService.JobWeight.HEAVY, heavyTaskExecutor));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.HEAVY, job -> uploadJob.rejectTask(job));
        executorService.setRejectJobHandler(SmartExecutorService.JobWeight.LIGHT, job -> uploadJob.rejectTask(job));

        return executorService;
    }
}
