package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.apache.http.client.config.RequestConfig.custom;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public ClientHttpRequestFactory requestFactory() {
        RequestConfig requestConfig = RequestConfig.copy(custom().build())
                .setSocketTimeout(2000)
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000).build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setMaxConnTotal(2000)
                .setMaxConnPerRoute(1000)
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(2000).build())
                .setDefaultRequestConfig(requestConfig);

        return new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder(ObjectProvider<HttpMessageConverters> messageConverters,
                                                   ObjectProvider<RestTemplateCustomizer> restTemplateCustomizers,
                                                   ObjectProvider<RestTemplateRequestCustomizer<?>> restTemplateRequestCustomizers,
                                                   ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder.requestFactory(() -> clientHttpRequestFactory);
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
