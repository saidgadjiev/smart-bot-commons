package ru.gadjini.telegram.smart.bot.commons.configuration;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplate(clientHttpRequestFactory);
    }
}
