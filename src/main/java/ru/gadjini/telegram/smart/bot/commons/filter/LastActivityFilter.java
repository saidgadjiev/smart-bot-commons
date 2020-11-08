package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;

@Component
public class LastActivityFilter extends BaseBotFilter {

    private UserService userService;

    @Autowired
    public LastActivityFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(Update update) {
        userService.activity(TgMessage.getUser(update));
        super.doFilter(update);
    }
}
