package ru.gadjini.telegram.smart.bot.commons.configuration;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.property.BotApiProperties;
import ru.gadjini.telegram.smart.bot.commons.property.WebhookProperties;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.utils.ReflectionUtils;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyBotSession;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyWebhook;

@Configuration
public class SmartBotConfiguration {

    public static final String PROFILE_PROD = "prod";

    public static final String PROFILE_DEV = "dev";

    public static final String PROFILE_LOAD_TEST = "load-test";

    //Infinite
    private static final int SO_TIMEOUT = 0;

    @Bean
    @Profile({PROFILE_PROD, PROFILE_LOAD_TEST})
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SetWebhook setWebhook(WebhookProperties webhookProperties) {
        return SetWebhook.builder().url(webhookProperties.getUrl() + "/callback").maxConnections(webhookProperties.getMaxConnections()).build();
    }

    @Bean
    @Profile({PROFILE_PROD, PROFILE_LOAD_TEST})
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DummyBotSession.class, new DummyWebhook());
    }

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
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer c = new PropertySourcesPlaceholderConfigurer();
        c.setLocation(new ClassPathResource("git.properties"));
        c.setIgnoreResourceNotFound(true);
        c.setIgnoreUnresolvablePlaceholders(true);

        return c;
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueJobConfigurator queueJobConfigurator() {
        return new QueueJobConfigurator() {
        };
    }
}
