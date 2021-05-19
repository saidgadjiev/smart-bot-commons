package org.telegram.telegrambots.facilities;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.facilities.proxysocketfactorys.HttpConnectionSocketFactory;
import org.telegram.telegrambots.facilities.proxysocketfactorys.HttpSSLConnectionSocketFactory;
import org.telegram.telegrambots.facilities.proxysocketfactorys.SocksConnectionSocketFactory;
import org.telegram.telegrambots.facilities.proxysocketfactorys.SocksSSLConnectionSocketFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by bvn13 on 17.04.2018.
 */
public class TelegramHttpClientBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramHttpClientBuilder.class);

    public static CloseableHttpClient build(DefaultBotOptions options) {
        int connPerRoute = 1000;
        int maxConnTotal = 2000;

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setConnectionManager(createConnectionManager(options))
                .setRetryHandler((exception, executionCount, context) -> {
                    if (executionCount > 3) {
                        LOGGER.warn("Maximum tries reached for client http pool ");
                        return false;
                    }
                    if (exception instanceof org.apache.http.NoHttpResponseException) {
                        LOGGER.warn("No response from server on " + executionCount + " call");
                        return true;
                    }
                    return false;
                })
                .setConnectionTimeToLive(70, TimeUnit.SECONDS)
                .setMaxConnTotal(maxConnTotal)
                .setMaxConnPerRoute(connPerRoute);

        LOGGER.debug("Custom http client({}, {})", connPerRoute, maxConnTotal);

        return httpClientBuilder.build();
    }

    private static HttpClientConnectionManager createConnectionManager(DefaultBotOptions options) {
        Registry<ConnectionSocketFactory> registry;
        switch (options.getProxyType()) {
            case NO_PROXY:
                return null;
            case HTTP:
                registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new HttpConnectionSocketFactory())
                        .register("https", new HttpSSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
                return new PoolingHttpClientConnectionManager(registry);
            case SOCKS4:
            case SOCKS5:
                registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", new SocksConnectionSocketFactory())
                        .register("https", new SocksSSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                        .build();
                return new PoolingHttpClientConnectionManager(registry);
        }
        return null;
    }

}
