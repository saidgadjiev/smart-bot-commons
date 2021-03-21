package ru.gadjini.telegram.smart.bot.commons.command.api;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;

public interface PaymentsHandler {

    void preCheckout(PreCheckoutQuery preCheckoutQuery);

    void successfulPayment(Message message);
}
