package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateFilter extends BaseBotFilter {

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()
                || update.hasCallbackQuery() && update.getCallbackQuery().getMessage().getChat().isUserChat()) {
            super.doFilter(update);
        }
    }
}
