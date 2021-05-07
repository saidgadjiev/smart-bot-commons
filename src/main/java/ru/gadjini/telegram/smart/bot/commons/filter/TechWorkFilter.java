package ru.gadjini.telegram.smart.bot.commons.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.AdminProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Component
public class TechWorkFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TechWorkFilter.class);

    @Value("${tech.work:false}")
    private boolean techWork;

    private MessageService messageService;

    private UserService userService;

    private LocalisationService localisationService;

    private AdminProperties adminProperties;

    @Autowired
    public TechWorkFilter(@TgMessageLimitsControl MessageService messageService, UserService userService,
                          LocalisationService localisationService, AdminProperties adminProperties) {
        this.messageService = messageService;
        this.userService = userService;
        this.localisationService = localisationService;
        this.adminProperties = adminProperties;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("Tech work({})", techWork);
    }

    @Override
    public void doFilter(Update update) {
        if (techWork) {
            if (!CollectionUtils.isEmpty(adminProperties.getWhiteList())) {
                long chatId = TgMessage.getChatId(update);

                if (!adminProperties.getWhiteList().contains(chatId)) {
                    techWork(update);
                } else {
                    super.doFilter(update);
                }
            } else {
                techWork(update);
            }
        } else {
            super.doFilter(update);
        }
    }

    private void techWork(Update update) {
        int userId = TgMessage.getUserId(update);
        Locale locale = userService.getLocaleOrDefault(userId);
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(userId))
                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_TECH_WORK, locale))
                        .build()
        );
    }
}
