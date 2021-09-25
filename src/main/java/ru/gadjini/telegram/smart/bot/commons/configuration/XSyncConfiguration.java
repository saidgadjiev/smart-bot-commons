package ru.gadjini.telegram.smart.bot.commons.configuration;

import com.antkorwin.xsync.XSync;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XSyncConfiguration {

    @Bean
    public XSync<Long> longXSync() {
        return new XSync<>();
    }

    @Bean
    @Qualifier("messagesQueue")
    public XSync<String> messagesQueueXSync() {
        return new XSync<>();
    }
}
