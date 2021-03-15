package ru.gadjini.telegram.smart.bot.commons.configuration;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.property.*;
import ru.gadjini.telegram.smart.bot.commons.service.queue.QueueJobConfigurator;
import ru.gadjini.telegram.smart.bot.commons.utils.ReflectionUtils;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyBotSession;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyWebhook;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Configuration
public class SmartBotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartBotConfiguration.class);

    public static final String PROFILE_PROD_PRIMARY = "prod-primary";

    public static final String PROFILE_PROD_SECONDARY = "prod-secondary";

    public static final String PROFILE_DEV_PRIMARY = "dev-primary";

    public static final String PROFILE_DEV_SECONDARY = "dev-secondary";

    public static final int PRIMARY_SERVER_NUMBER = 1;

    //Infinite
    private static final int SO_TIMEOUT = 0;

    @Autowired
    public SmartBotConfiguration(ServerProperties serverProperties, JobsProperties jobsProperties, AdminProperties adminProperties) {
        LOGGER.debug("Server number({})", serverProperties.getNumber());
        LOGGER.debug("Servers({})", serverProperties.getServers());
        LOGGER.debug("Disable jobs({})", jobsProperties.isDisable());
        LOGGER.debug("Enable jobs logging({})", jobsProperties.isEnableLogging());
        LOGGER.debug("Download upload synchronizer jobs logging({})", jobsProperties.isEnableDownloadUploadSynchronizerLogging());
        LOGGER.debug("Admin white list({})", adminProperties.getWhiteList());
    }

    @Bean
    @Profile({PROFILE_PROD_PRIMARY})
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SetWebhook setWebhook(WebhookProperties webhookProperties) {
        return SetWebhook.builder().url(webhookProperties.getUrl() + "/callback").maxConnections(webhookProperties.getMaxConnections()).build();
    }

    @Bean
    @Profile({PROFILE_PROD_PRIMARY})
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

    @Bean
    public RestTemplateBuilder restTemplateBuilder(ObjectProvider<HttpMessageConverters> messageConverters,
                                                   ObjectProvider<RestTemplateCustomizer> restTemplateCustomizers,
                                                   ObjectProvider<RestTemplateRequestCustomizer<?>> restTemplateRequestCustomizers) {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder.setReadTimeout(Duration.of(2, ChronoUnit.SECONDS));
        builder.setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS));

        HttpMessageConverters converters = messageConverters.getIfUnique();
        if (converters != null) {
            builder = builder.messageConverters(converters.getConverters());
        }
        builder = addCustomizers(builder, restTemplateCustomizers, RestTemplateBuilder::customizers);
        builder = addCustomizers(builder, restTemplateRequestCustomizers, RestTemplateBuilder::requestCustomizers);
        return builder;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    private <T> RestTemplateBuilder addCustomizers(RestTemplateBuilder builder, ObjectProvider<T> objectProvider,
                                                   BiFunction<RestTemplateBuilder, Collection<T>, RestTemplateBuilder> method) {
        List<T> customizers = objectProvider.orderedStream().collect(Collectors.toList());
        if (!customizers.isEmpty()) {
            return method.apply(builder, customizers);
        }
        return builder;
    }
}
