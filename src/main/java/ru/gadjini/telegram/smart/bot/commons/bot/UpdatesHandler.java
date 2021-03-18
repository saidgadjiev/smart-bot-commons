package ru.gadjini.telegram.smart.bot.commons.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdatesHandler {

    void onUpdateReceived(Update update);
}
