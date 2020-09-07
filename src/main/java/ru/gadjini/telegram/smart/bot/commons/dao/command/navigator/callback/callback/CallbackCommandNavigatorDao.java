package ru.gadjini.telegram.smart.bot.commons.dao.command.navigator.callback.callback;

public interface CallbackCommandNavigatorDao {
    void set(long chatId, String command);

    String get(long chatId);

    void delete(long chatId);
}
