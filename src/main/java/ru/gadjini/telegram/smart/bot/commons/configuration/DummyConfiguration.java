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
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.DummyCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.DefaultHelpCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.DefaultStartCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.HelpCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.StartCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.DummyReplyKeyboardHolderService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.SmartReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.CheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.DefaultCheckPaidSubscriptionMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionPlanService;

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

    @Bean
    @ConditionalOnMissingBean(CheckPaidSubscriptionMessageBuilder.class)
    public DefaultCheckPaidSubscriptionMessageBuilder checkPaidSubscriptionMessageBuilder(
            PaidSubscriptionPlanService paidSubscriptionPlanService,
            LocalisationService localisationService,
            SubscriptionProperties subscriptionProperties
    ) {
        return new DefaultCheckPaidSubscriptionMessageBuilder(paidSubscriptionPlanService, localisationService, subscriptionProperties);
    }

    @Bean
    @ConditionalOnMissingBean(HelpCommandMessageBuilder.class)
    public HelpCommandMessageBuilder helpCommandMessageBuilder(LocalisationService localisationService,
                                                               CommandMessageBuilder commandMessageBuilder) {
        return new DefaultHelpCommandMessageBuilder(localisationService, commandMessageBuilder);
    }

    @Bean
    @ConditionalOnMissingBean(StartCommandMessageBuilder.class)
    public StartCommandMessageBuilder startCommandMessageBuilder(LocalisationService localisationService,
                                                                 CommandMessageBuilder commandMessageBuilder) {
        return new DefaultStartCommandMessageBuilder(localisationService, commandMessageBuilder);
    }
}
