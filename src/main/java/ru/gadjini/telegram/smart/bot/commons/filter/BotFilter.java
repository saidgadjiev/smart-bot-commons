package ru.gadjini.telegram.smart.bot.commons.filter;

import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Update;

public interface BotFilter {

    BotFilter setNext(BotFilter next);

    void doFilter(Update update);
}
