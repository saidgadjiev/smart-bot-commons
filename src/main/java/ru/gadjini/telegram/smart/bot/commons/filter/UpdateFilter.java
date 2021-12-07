package ru.gadjini.telegram.smart.bot.commons.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.property.UpdateFilterProperties;

@Component
public class UpdateFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFilter.class);

    private UpdateFilterProperties updateFilterProperties;

    @Autowired
    public UpdateFilter(UpdateFilterProperties updateFilterProperties) {
        this.updateFilterProperties = updateFilterProperties;
    }

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()
                && !update.getMessage().getFrom().getIsBot()
                || update.hasCallbackQuery() && update.getCallbackQuery().getMessage().getChat().isUserChat()
                || doAdditionalFiltering(update)
        ) {
            super.doFilter(update);
        } else {
            LOGGER.debug("Request can'be accepted({})", update);
        }
    }

    private boolean doAdditionalFiltering(Update update) {
        return updateFilterProperties.isAcceptPayments() && update.hasPreCheckoutQuery();
    }
}
