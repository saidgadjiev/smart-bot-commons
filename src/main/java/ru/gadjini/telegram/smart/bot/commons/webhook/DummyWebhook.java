package ru.gadjini.telegram.smart.bot.commons.webhook;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.meta.generics.WebhookBot;

public class DummyWebhook implements Webhook {

    @Override
    public void startServer() throws TelegramApiException {

    }

    @Override
    public void registerWebhook(WebhookBot callback) {

    }

    @Override
    public void setInternalUrl(String internalUrl) {

    }

    @Override
    public void setKeyStore(String keyStore, String keyStorePassword) throws TelegramApiException {

    }
}
