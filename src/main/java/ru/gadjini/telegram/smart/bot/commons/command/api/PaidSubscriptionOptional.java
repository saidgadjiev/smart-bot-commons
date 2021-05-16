package ru.gadjini.telegram.smart.bot.commons.command.api;

public interface PaidSubscriptionOptional extends SmartBotCommand {

    @Override
    default boolean isPaidSubscriptionRequired() {
        return false;
    }
}
