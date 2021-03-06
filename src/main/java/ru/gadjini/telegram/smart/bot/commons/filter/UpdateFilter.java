package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.AdminProperties;

@Component
public class UpdateFilter extends BaseBotFilter {

    private AdminProperties adminProperties;

    @Autowired
    public UpdateFilter(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()
                || update.hasCallbackQuery() && update.getCallbackQuery().getMessage().getChat().isUserChat()) {
            if (!CollectionUtils.isEmpty(adminProperties.getWhiteList())) {
                long chatId = TgMessage.getChatId(update);

                if (!adminProperties.getWhiteList().contains(chatId)) {
                    return;
                }
            }

            super.doFilter(update);
        }
    }
}
