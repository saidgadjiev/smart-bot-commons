package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("http.client")
public class HttpClientProperties {

    private int downloadRequestTimeout = 5 * 60 * 1000;

    public int getDownloadRequestTimeout() {
        return downloadRequestTimeout;
    }

    public void setDownloadRequestTimeout(int downloadRequestTimeout) {
        this.downloadRequestTimeout = downloadRequestTimeout;
    }
}
