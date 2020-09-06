package ru.gadjini.telegram.smart.bot.commons.dao.command.keyboard;

import org.springframework.stereotype.Repository;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.ReplyKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository("inMemory")
public class InMemoryReplyKeyboardDao implements ReplyKeyboardDao {

    private Map<Long, ReplyKeyboardMarkup> cache = new ConcurrentHashMap<>();

    @Override
    public void store(long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        cache.put(chatId, replyKeyboardMarkup);
    }

    @Override
    public ReplyKeyboardMarkup get(long chatId) {
        return cache.get(chatId);
    }
}
