package ru.gadjini.telegram.smart.bot.commons.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot")
public class BotProperties {

    private String token;

    private String name;

    private boolean logout;

    private boolean close;

    private boolean clearWebhook;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLogout() {
        return logout;
    }

    public void setLogout(boolean logout) {
        this.logout = logout;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public boolean isClearWebhook() {
        return clearWebhook;
    }

    public void setClearWebhook(boolean clearWebhook) {
        this.clearWebhook = clearWebhook;
    }
}

