package ru.gadjini.telegram.smart.bot.commons.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.property.UpdateFilterProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

import java.util.Locale;

@Component
public class UpdateFilter extends BaseBotFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFilter.class);

    private UpdateFilterProperties updateFilterProperties;

    private MessageService messageService;

    private LocalisationService localisationService;

    @Autowired
    public UpdateFilter(UpdateFilterProperties updateFilterProperties,
                        @TgMessageLimitsControl MessageService messageService, LocalisationService localisationService) {
        this.updateFilterProperties = updateFilterProperties;
        this.messageService = messageService;
        this.localisationService = localisationService;
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
            messageService.sendMessage(SendMessage.builder()
                    .text(localisationService.getMessage(MessagesProperties.MESSAGE_CANT_ACCEPT_REQUEST, Locale.getDefault()))
                    .build());
        }
    }

    private boolean doAdditionalFiltering(Update update) {
        return updateFilterProperties.isAcceptPayments() && update.hasPreCheckoutQuery();
    }
}
