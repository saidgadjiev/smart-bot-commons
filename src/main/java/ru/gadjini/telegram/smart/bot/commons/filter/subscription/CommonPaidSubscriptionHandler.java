package ru.gadjini.telegram.smart.bot.commons.filter.subscription;

import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.domain.PaidSubscription;

@Component
public class CommonPaidSubscriptionHandler {

    public boolean isActiveSubscription(PaidSubscription subscription) {
        return subscription != null && !subscription.isTrial()
                && (subscription.isActive() || subscription.isSubscriptionIntervalActive());
    }
}
