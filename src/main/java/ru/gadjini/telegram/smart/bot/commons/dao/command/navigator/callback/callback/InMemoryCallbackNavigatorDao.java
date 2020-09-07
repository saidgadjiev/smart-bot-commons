package ru.gadjini.telegram.smart.bot.commons.dao.command.navigator.callback.callback;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Qualifier("inMemory")
public class InMemoryCallbackNavigatorDao implements CallbackCommandNavigatorDao {

    private Map<Long, String> commands = new ConcurrentHashMap<>();

    @Override
    public void set(long chatId, String command) {
        commands.put(chatId, command);
    }

    @Override
    public String get(long chatId) {
        return commands.get(chatId);
    }

    @Override
    public void delete(long chatId) {
        commands.remove(chatId);
    }
}
