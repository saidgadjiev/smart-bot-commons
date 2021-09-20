package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot.api.balancer")
public class TelegramBotApiBalancerProperties {

    @Value("${downloading.light.file.max.size:18000000}")
    private long downloadingLightFileMaxSize;

    @Value("${uploading.light.file.max.size:48000000}")
    private long uploadingLightFileMaxSize;

    public long getDownloadingLightFileMaxSize() {
        return downloadingLightFileMaxSize;
    }

    public void setDownloadingLightFileMaxSize(long downloadingLightFileMaxSize) {
        this.downloadingLightFileMaxSize = downloadingLightFileMaxSize;
    }

    public long getUploadingLightFileMaxSize() {
        return uploadingLightFileMaxSize;
    }

    public void setUploadingLightFileMaxSize(long uploadingLightFileMaxSize) {
        this.uploadingLightFileMaxSize = uploadingLightFileMaxSize;
    }
}
