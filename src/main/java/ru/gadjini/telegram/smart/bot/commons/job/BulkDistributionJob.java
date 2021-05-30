package ru.gadjini.telegram.smart.bot.commons.job;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.net.SocketException;
import java.net.SocketTimeoutException;

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

    public void distribute() {
        if (disable) {
            return;
        }
        BulkDistribution bulkDistribution = bulkDistributionService.getDistribution(botProperties.getName());
        if (bulkDistribution == null) {
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
            if (!isNoneCriticalDownloadingException(e)) {
                userService.handleBotBlockedByUser(e);
                bulkDistributionService.delete(bulkDistribution.getId());
            }
        }
    }

    private static boolean isNoneCriticalDownloadingException(Throwable ex) {
        int indexOfNoResponseException = ExceptionUtils.indexOfThrowable(ex, NoHttpResponseException.class);
        int socketException = ExceptionUtils.indexOfThrowable(ex, SocketException.class);
        int socketTimeOutException = ExceptionUtils.indexOfThrowable(ex, SocketTimeoutException.class);
        boolean restart500 = false;
        if (StringUtils.isNotBlank(ex.getMessage())) {
            restart500 = ex.getMessage().contains("Internal Server Error: restart");
        }

        return indexOfNoResponseException != -1 || socketException != -1
                || socketTimeOutException != -1 || restart500;
    }
}
