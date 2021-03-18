package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.bot.UpdatesHandler;

@Component
public class UpdatesHandlerFilter extends BaseBotFilter {

    private UpdatesHandler updatesHandler;

    @Autowired
    public UpdatesHandlerFilter(UpdatesHandler updatesHandler) {
        this.updatesHandler = updatesHandler;
    }

    @Override
    public void doFilter(Update update) {
        updatesHandler.onUpdateReceived(update);
    }
}
