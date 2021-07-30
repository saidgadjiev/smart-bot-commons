package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.telegram.telegrambots.Constants;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gadjini.telegram.smart.bot.commons.common.Profiles;
import ru.gadjini.telegram.smart.bot.commons.filter.subscription.ExpiredPaidSubscriptionHandler;
import ru.gadjini.telegram.smart.bot.commons.property.*;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionService;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.tariff.PaidSubscriptionTariffType;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiMediaService;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramBotApiMethodExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TelegramMediaService;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyBotSession;
import ru.gadjini.telegram.smart.bot.commons.webhook.DummyWebhook;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class SmartBotConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartBotConfiguration.class);

    @Autowired
    public SmartBotConfiguration(AdminProperties adminProperties,
                                 SubscriptionProperties subscriptionProperties,
                                 BotApiProperties botApiProperties,
                                 HttpClientProperties httpClientProperties) {
        LOGGER.debug("Local work dir({})", botApiProperties.getLocalWorkDir());
        LOGGER.debug("Work dir({})", botApiProperties.getWorkDir());
        LOGGER.debug("Admin white list({})", adminProperties.getWhiteList());
        LOGGER.debug("Channel subscription({})", subscriptionProperties.isCheckChannelSubscription());
        LOGGER.debug("Paid subscription({})", subscriptionProperties.isCheckPaidSubscription());
        LOGGER.debug("Payment bot({})", subscriptionProperties.getPaymentBotName());
        LOGGER.debug("Paid bot({})", subscriptionProperties.getPaidBotName());
        LOGGER.debug("Trial period({})", subscriptionProperties.getTrialPeriod());
        LOGGER.debug("Http download request timeout({})", httpClientProperties.getDownloadRequestTimeout());
    }

    @Bean
    @Profile({Profiles.PROFILE_PROD_PRIMARY})
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SetWebhook setWebhook(WebhookProperties webhookProperties) {
        return SetWebhook.builder().url(webhookProperties.getUrl()).maxConnections(webhookProperties.getMaxConnections()).build();
    }

    @Bean
    @Profile({Profiles.PROFILE_PROD_PRIMARY})
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
                RequestConfig.custom()
                        //TODO: infinite
                        .setSocketTimeout(0)
                        .setConnectTimeout(Constants.SOCKET_TIMEOUT)
                        .setConnectionRequestTimeout(Constants.SOCKET_TIMEOUT).build());

        return defaultBotOptions;
    }

    @Bean
    @Scope
    @Qualifier("downloadRequestConfig")
    public RequestConfig downloadRequestConfig(HttpClientProperties httpClientProperties) {
        return RequestConfig.custom()
                .setSocketTimeout(httpClientProperties.getDownloadRequestTimeout())
                .setConnectTimeout(Constants.SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(Constants.SOCKET_TIMEOUT).build();
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
    public TelegramMediaService telegramMediaService(BotProperties botProperties,
                                                     DefaultBotOptions options, BotApiProperties botApiProperties,
                                                     TelegramBotApiMethodExecutor exceptionHandler) {
        return new TelegramBotApiMediaService(botProperties, options, botApiProperties, exceptionHandler);
    }

    @Bean
    public Map<PaidSubscriptionTariffType, PaidSubscriptionService> tariffServiceMap(
            Set<PaidSubscriptionService> paidSubscriptionServices
    ) {
        Map<PaidSubscriptionTariffType, PaidSubscriptionService> tariffServiceMap = new HashMap<>();
        for (PaidSubscriptionService paidSubscriptionService : paidSubscriptionServices) {
            tariffServiceMap.put(paidSubscriptionService.tariffType(), paidSubscriptionService);
        }

        return tariffServiceMap;
    }

    @Bean
    public Map<PaidSubscriptionTariffType, ExpiredPaidSubscriptionHandler> expiredPaidSubscriptionHandlerMap(
            Set<ExpiredPaidSubscriptionHandler> expiredPaidSubscriptionHandlers
    ) {
        Map<PaidSubscriptionTariffType, ExpiredPaidSubscriptionHandler> expiredPaidSubscriptionHandlerMap = new HashMap<>();
        for (ExpiredPaidSubscriptionHandler expiredPaidSubscriptionHandler : expiredPaidSubscriptionHandlers) {
            expiredPaidSubscriptionHandlerMap.put(expiredPaidSubscriptionHandler.tariffType(), expiredPaidSubscriptionHandler);
        }

        return expiredPaidSubscriptionHandlerMap;
    }
}
