package ru.gadjini.telegram.smart.bot.commons.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.domain.BulkDistribution;
import ru.gadjini.telegram.smart.bot.commons.job.BulkDistributionJob;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.distribution.BulkDistributionService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.user.UserBotService;

@Component
public class DistributionFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionFilter.class);

    private BulkDistributionJob bulkDistributionJob;

    private BulkDistributionService bulkDistributionService;

    private BotProperties botProperties;

    private MessageService messageService;

    private UserBotService userBotService;

    private UserService userService;

    @Autowired
    public DistributionFilter(BulkDistributionJob bulkDistributionJob, BulkDistributionService bulkDistributionService,
                              BotProperties botProperties, @TgMessageLimitsControl MessageService messageService,
                              UserBotService userBotService, UserService userService) {
        this.bulkDistributionJob = bulkDistributionJob;
        this.bulkDistributionService = bulkDistributionService;
        this.botProperties = botProperties;
        this.messageService = messageService;
        this.userBotService = userBotService;
        this.userService = userService;
    }

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() && !bulkDistributionJob.isDisable()) {
            BulkDistribution bulkDistribution = bulkDistributionService.deleteAndGetDistribution(
                    update.getMessage().getFrom().getId(), botProperties.getName());

            if (bulkDistribution != null) {
                try {
                    messageService.sendMessage(
                            SendMessage.builder()
                                    .chatId(String.valueOf(bulkDistribution.getUserId()))
                                    .text(bulkDistribution.getMessage(userService.getLocaleOrDefault(bulkDistribution.getUserId())))
                                    .parseMode(ParseMode.HTML)
                                    .build()
                    );
                    userBotService.create(bulkDistribution.getUserId(), botProperties.getName());
                    return;
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        super.doFilter(update);
    }
}
