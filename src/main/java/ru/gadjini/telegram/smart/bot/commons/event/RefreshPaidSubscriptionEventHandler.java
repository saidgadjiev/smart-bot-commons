package ru.gadjini.telegram.smart.bot.commons.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.service.subscription.PaidSubscriptionRemoveService;

@Component
public class RefreshPaidSubscriptionEventHandler {

    private PaidSubscriptionRemoveService paidSubscriptionRemoveService;

    @Autowired
    public RefreshPaidSubscriptionEventHandler(PaidSubscriptionRemoveService paidSubscriptionRemoveService) {
        this.paidSubscriptionRemoveService = paidSubscriptionRemoveService;
    }

    @EventListener
    public void handle(RefreshPaidSubscriptionEvent event) {
        paidSubscriptionRemoveService.refreshPaidSubscription(event.getUserId());
    }
}
