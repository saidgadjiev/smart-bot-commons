package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface NavigablePaidChannelSubscriptionOptional extends NavigableBotCommand {

    @Override
    default boolean isChannelSubscriptionRequired(Message message) {
        return false;
    }

    @Override
    default boolean isPaidSubscriptionRequired(Message message) {
        return false;
    }
}
