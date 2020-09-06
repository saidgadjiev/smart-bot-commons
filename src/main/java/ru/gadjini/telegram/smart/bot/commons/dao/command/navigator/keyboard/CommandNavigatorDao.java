package ru.gadjini.telegram.smart.bot.commons.dao.command.navigator.keyboard;

public interface CommandNavigatorDao {
    void set(long chatId, String command);

    String get(long chatId);

}
