package ru.gadjini.telegram.smart.bot.commons.filter;

import com.antkorwin.xsync.XSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;

@Component
public class UserSynchronizedFilter extends BaseBotFilter {

    private XSync<Long> longXSync;

    @Autowired
    public UserSynchronizedFilter(XSync<Long> longXSync) {
        this.longXSync = longXSync;
    }

    @Override
    public void doFilter(Update update) {
        long chatId = TgMessage.getChatId(update);

        longXSync.execute(chatId, () -> super.doFilter(update));
    }
}
