package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface SmartBotCommand {

    default boolean accept(Message message) {
        return true;
    }

    default boolean isChannelSubscriptionRequired() {
        return true;
    }

    default boolean isPaidSubscriptionRequired() {
        return true;
    }
}
