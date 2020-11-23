package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.QueueItem;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.queue.WorkQueueService;

import java.util.Locale;

@Component
public class QueryStatsCommand implements BotCommand {

    private WorkQueueService queueService;

    private LocalisationService localisationService;

    private UserService userService;

    private MessageService messageService;

    @Autowired
    public QueryStatsCommand(WorkQueueService queueService, LocalisationService localisationService,
                             UserService userService, @Qualifier("messageLimits") MessageService messageService) {
        this.queueService = queueService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        long processing = queueService.countByStatusAllTime(QueueItem.Status.PROCESSING);
        long waiting = queueService.countByStatusAllTime(QueueItem.Status.WAITING);
        long errorForToday = queueService.countByStatusForToday(QueueItem.Status.EXCEPTION);
        long errorAllTime = queueService.countByStatusAllTime(QueueItem.Status.EXCEPTION);
        long completed = queueService.countByStatusForToday(QueueItem.Status.COMPLETED);
        long activeUsers = queueService.countActiveUsersForToday();

        String statsMessage = localisationService.getMessage(MessagesProperties.MESSAGE_QUEUE_STATS, new Object[]{
                processing, waiting, errorForToday, completed, activeUsers, errorAllTime
        }, locale);
        messageService.sendMessage(SendMessage.builder().chatId(String.valueOf(message.getChatId()))
                .text(statsMessage).parseMode(ParseMode.HTML).build());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.QUEUE_STATS;
    }
}
