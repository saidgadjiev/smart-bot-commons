package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;

@Configuration
public class SmartBotConfiguration {

    //Infinite
    private static final int SO_TIMEOUT = 0;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DefaultBotOptions botOptions(BotApiProperties localBotApiProperties) {
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();

        if (StringUtils.isNotBlank(localBotApiProperties.getEndpoint())) {
            defaultBotOptions.setBaseUrl(localBotApiProperties.getEndpoint());
        }
        defaultBotOptions.setRequestConfig(
                RequestConfig.copy(RequestConfig.custom().build())
                        .setSocketTimeout(SO_TIMEOUT)
                        .setConnectTimeout(SO_TIMEOUT)
                        .setConnectionRequestTimeout(SO_TIMEOUT).build());

        return defaultBotOptions;
    }
}
