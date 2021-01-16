package ru.gadjini.telegram.smart.bot.commons.webhook;

import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

public class DummyBotSession implements BotSession {
    @Override
    public void setOptions(BotOptions options) {

    }

    @Override
    public void setToken(String token) {

    }

    @Override
    public void setCallback(LongPollingBot callback) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
