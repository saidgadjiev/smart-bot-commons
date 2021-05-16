package ru.gadjini.telegram.smart.bot.commons.command.api;

public interface PaidChannelSubscriptionOptional extends SmartBotCommand {

    default boolean isChannelSubscriptionRequired() {
        return false;
    }

    default boolean isPaidSubscriptionRequired() {
        return false;
    }
}
