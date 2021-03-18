package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.DummyCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.DummyReplyKeyboardHolderService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;

@Configuration
public class DummyConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommandMessageBuilder.class)
    public CommandMessageBuilder commandMessageBuilder() {
        return new DummyCommandMessageBuilder();
    }

    @Bean
    @KeyboardHolder
    @ConditionalOnMissingBean(annotation = KeyboardHolder.class)
    public ReplyKeyboardService replyKeyboardService() {
        return new DummyReplyKeyboardHolderService();
    }
}
