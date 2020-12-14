package ru.gadjini.telegram.smart.bot.commons.configuration;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.utils.ReflectionUtils;

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

    @Bean
    @Qualifier("botapi")
    public GsonBuilder gsonBuilder() {
        return new GsonBuilder().serializeNulls()
                .registerTypeAdapter(ReplyKeyboard.class, (JsonSerializer<ReplyKeyboard>) (replyKeyboard, type, jsonSerializationContext) -> {
                    JsonElement jsonElement = jsonSerializationContext.serialize(replyKeyboard);
                    jsonElement.getAsJsonObject().addProperty("class", replyKeyboard.getClass().getName());

                    return jsonElement;
                })
                .registerTypeAdapter(ReplyKeyboard.class, (JsonDeserializer<Object>) (jsonElement, type, jsonDeserializationContext) -> {
                    String aClass = jsonElement.getAsJsonObject().get("class").getAsString();
                    Class<?> clazz = ReflectionUtils.getClass(aClass);

                    return jsonDeserializationContext.deserialize(jsonElement, clazz);
                });
    }

    @Bean
    @Qualifier("botapi")
    public Gson gson(GsonBuilder gsonBuilder) {
        return gsonBuilder.create();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueJobConfigurator queueJobConfigurator() {
        return new QueueJobConfigurator() {
        };
    }
}
