package ru.gadjini.telegram.smart.bot.commons.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.property.UpdateFilterProperties;
import ru.gadjini.telegram.smart.bot.commons.service.user.BlackListService;

@Component
public class UpdateFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFilter.class);

    private UpdateFilterProperties updateFilterProperties;

    private ObjectMapper objectMapper;

    private BlackListService blackListService;

    @Autowired
    public UpdateFilter(UpdateFilterProperties updateFilterProperties, ObjectMapper objectMapper, BlackListService blackListService) {
        this.updateFilterProperties = updateFilterProperties;
        this.objectMapper = objectMapper;
        this.blackListService = blackListService;
    }

    @Override
    public void doFilter(Update update) {
        Long userId = TgMessage.getUserId(update);
        if (userId != null && blackListService.isInBlackList(userId)) {
            return;
        }
        if (update.hasMessage() && update.getMessage().getChat().isUserChat()
                && !update.getMessage().getFrom().getIsBot()
                || update.hasCallbackQuery() && update.getCallbackQuery().getMessage().getChat().isUserChat()
                || doAdditionalFiltering(update)
        ) {
            super.doFilter(update);
        } else {
            try {
                LOGGER.debug("Request can'be accepted({})", objectMapper.writeValueAsString(update));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private boolean doAdditionalFiltering(Update update) {
        return updateFilterProperties.isAcceptPayments() && update.hasPreCheckoutQuery();
    }
}
