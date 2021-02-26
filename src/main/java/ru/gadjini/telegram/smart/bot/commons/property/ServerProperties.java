package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.gadjini.telegram.smart.bot.commons.configuration.SmartBotConfiguration;

@ConfigurationProperties("server")
public class ServerProperties {

    private int number = SmartBotConfiguration.PRIMARY_SERVER_NUMBER;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
