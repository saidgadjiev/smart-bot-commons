package ru.gadjini.telegram.smart.bot.commons.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.domain.BulkDistribution;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.distribution.BulkDistributionService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.user.UserBotService;

@Component
public class BulkDistributionJob {

    private BulkDistributionService bulkDistributionService;

    private MessageService messageService;

    private UserService userService;

    private UserBotService userBotService;

    private BotProperties botProperties;

    private boolean disable;

    @Autowired
    public BulkDistributionJob(BulkDistributionService bulkDistributionService,
                               @TgMessageLimitsControl MessageService messageService,
                               UserService userService, UserBotService userBotService, BotProperties botProperties) {
        this.bulkDistributionService = bulkDistributionService;
        this.messageService = messageService;
        this.userService = userService;
        this.userBotService = userBotService;
        this.botProperties = botProperties;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    @Scheduled(fixedDelay = 1000 * 1000)
    public void distribute() {
        if (disable) {
            return;
        }
        BulkDistribution bulkDistribution = bulkDistributionService.getDistribution(botProperties.getName());
        if (bulkDistribution == null) {
            disable = true;
            return;
        }

        try {
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(bulkDistribution.getUserId()))
                            .text(bulkDistribution.getMessage(userService.getLocaleOrDefault(bulkDistribution.getUserId())))
                            .parseMode(ParseMode.HTML)
                            .build()
            );
            userBotService.create(bulkDistribution.getUserId(), botProperties.getName());
            bulkDistributionService.delete(bulkDistribution.getId());
        } catch (FloodWaitException ignore) {
            userBotService.create(bulkDistribution.getUserId(), botProperties.getName());
        } catch (Throwable e) {
            userService.handleBotBlockedByUser(e);
            bulkDistributionService.delete(bulkDistribution.getId());
        }
    }
}
