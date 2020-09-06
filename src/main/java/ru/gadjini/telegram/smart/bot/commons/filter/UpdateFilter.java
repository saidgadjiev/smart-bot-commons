package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Update;

@Component
public class UpdateFilter extends BaseBotFilter {

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            super.doFilter(update);
        }
    }
}
