package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.concurrent.SmartExecutorService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.Map;

@Component
public class WorkThreadPoolStatsCommand implements BotCommand {

    private SmartExecutorService executorService;

    private LocalisationService localisationService;

    private WorkQueueService workQueueService;

    private MessageService messageService;

    private UserService userService;

    @Autowired
    public WorkThreadPoolStatsCommand(LocalisationService localisationService, @Qualifier("messageLimits") MessageService messageService,
                                      UserService userService) {
        this.localisationService = localisationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Autowired
    public void setWorkQueueService(WorkQueueService queueService) {
        this.workQueueService = queueService;
    }

    @Autowired
    public void setExecutorService(@Qualifier("queueTaskExecutor") SmartExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        int heavyCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.HEAVY);
        int lightCorePoolSize = executorService.getCorePoolSize(SmartExecutorService.JobWeight.LIGHT);
        int heavyActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.HEAVY).getActiveCount();
        int lightActiveCount = executorService.getExecutor(SmartExecutorService.JobWeight.LIGHT).getActiveCount();

        long processingHeavy = workQueueService.countProcessing(SmartExecutorService.JobWeight.HEAVY);
        long processingLight = workQueueService.countProcessing(SmartExecutorService.JobWeight.LIGHT);

        long readyToCompleteHeavy = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.HEAVY);
        long readToCompleteLight = workQueueService.countReadToComplete(SmartExecutorService.JobWeight.LIGHT);

        Map<Integer, SmartExecutorService.Job> activeTasks = executorService.getActiveTasks();
        StringBuilder activeTasksToString = new StringBuilder();
        activeTasks.forEach((integer, job) -> activeTasksToString.append(integer).append("-").append(job.getWeight().name()).append(" "));

        messageService.sendMessage(
                new SendMessage(
                        String.valueOf(message.getChatId()),
                        localisationService.getMessage(MessagesProperties.MESSAGE_WORK_THREAD_POOL_STATS, new Object[]{
                                heavyCorePoolSize, lightCorePoolSize, heavyActiveCount, lightActiveCount,
                                processingHeavy, processingLight, readyToCompleteHeavy, readToCompleteLight,
                                activeTasksToString.toString().trim()
                        }, userService.getLocaleOrDefault(message.getFrom().getId()))
                )
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.TSTATS;
    }
}
