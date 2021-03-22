package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.annotation.CommandStart;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.impl.StartCommand;
import ru.gadjini.telegram.smart.bot.commons.dao.command.keyboard.ReplyKeyboardDao;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.DummyCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.DummyReplyKeyboardHolderService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Configuration
public class DummyConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommandMessageBuilder.class)
    public CommandMessageBuilder commandMessageBuilder(LocalisationService localisationService) {
        return new DummyCommandMessageBuilder(localisationService);
    }

    @Bean
    @KeyboardHolder
    @ConditionalOnMissingBean(annotation = KeyboardHolder.class)
    public ReplyKeyboardService replyKeyboardService(@Redis ReplyKeyboardDao replyKeyboardDao,
                                                     SmartReplyKeyboardService smartReplyKeyboardService) {
        return new DummyReplyKeyboardHolderService(replyKeyboardDao, smartReplyKeyboardService);
    }

    @Bean
    @CommandStart
    @ConditionalOnMissingBean(annotation = CommandStart.class)
    public StartCommand startCommand(@TgMessageLimitsControl MessageService messageService, LocalisationService localisationService,
                                     UserService userService, @KeyboardHolder ReplyKeyboardService replyKeyboardService) {
        return new StartCommand(messageService, localisationService, userService, replyKeyboardService);
    }
}
