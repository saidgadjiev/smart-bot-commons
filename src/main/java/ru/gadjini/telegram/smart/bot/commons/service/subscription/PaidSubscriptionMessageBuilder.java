package ru.gadjini.telegram.smart.bot.commons.service.subscription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.property.SubscriptionProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.utils.MemoryUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.NumberUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.TimeUtils;

import java.time.ZonedDateTime;
import java.util.Locale;

@Component
public class PaidSubscriptionMessageBuilder {

    private LocalisationService localisationService;

    private SubscriptionProperties paidSubscriptionProperties;

    @Autowired
    public PaidSubscriptionMessageBuilder(LocalisationService localisationService,
                                          SubscriptionProperties paidSubscriptionProperties) {
        this.localisationService = localisationService;
        this.paidSubscriptionProperties = paidSubscriptionProperties;
    }

    public Builder builder(String rootMessage) {
        return new Builder(rootMessage);
    }

    public class Builder {

        private String rootMessage;

        private boolean withSubscriptionInstructions;

        private boolean withUtcTime;

        private boolean withPurchaseDate;

        private boolean withSubscriptionFor;

        private boolean withCheckSubscriptionCommand;

        private boolean withRenewInstructions;

        private boolean withPaidSubscriptionFeatures;

        private boolean withPaidSubscriptionAccesses;

        private boolean withTrialSubscriptionAccesses;

        private ZonedDateTime purchaseDate;

        private double minPrice;

        Builder(String rootMessage) {
            this.rootMessage = rootMessage;
        }

        public Builder withUtcTime() {
            this.withUtcTime = true;

            return this;
        }

        public Builder withPaidSubscriptionFeatures() {
            this.withPaidSubscriptionFeatures = true;

            return this;
        }

        public Builder withTrialSubscriptionAccesses() {
            this.withTrialSubscriptionAccesses = true;

            return this;
        }

        public Builder withPaidSubscriptionAccesses() {
            this.withPaidSubscriptionAccesses = true;

            return this;
        }

        public Builder withRenewInstructions() {
            this.withRenewInstructions = true;

            return this;
        }

        public Builder withSubscriptionInstructions(double minPrice) {
            this.withSubscriptionInstructions = true;
            this.minPrice = minPrice;

            return this;
        }

        public Builder withSubscriptionFor() {
            this.withSubscriptionFor = true;

            return this;
        }

        public Builder withPurchaseDate(ZonedDateTime purchaseDate) {
            this.withPurchaseDate = true;
            this.purchaseDate = purchaseDate;
            return this;
        }

        public Builder withCheckSubscriptionCommand() {
            this.withCheckSubscriptionCommand = true;

            return this;
        }

        public String buildMessage(Locale locale) {
            StringBuilder message = new StringBuilder();
            message.append(rootMessage);

            if (withSubscriptionFor || withUtcTime || withSubscriptionInstructions || withPurchaseDate
                    || withCheckSubscriptionCommand || withRenewInstructions) {
                message.append("\n\n");
            }

            if (withSubscriptionFor) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FOR, locale));
            }

            if (withPurchaseDate) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n");
                }

                message.append(localisationService.getMessage(
                        MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_PURCHASE_DATE,
                        new Object[]{
                                FixedTariffPaidSubscriptionService.HTML_PAID_SUBSCRIPTION_END_DATE_FORMATTER.format(purchaseDate)
                        },
                        locale
                ));
            }

            if (withUtcTime) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n");
                }

                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_UTC_TIME,
                        new Object[]{TimeUtils.TIME_FORMATTER.format(ZonedDateTime.now(TimeUtils.UTC))}, locale));
            }

            if (withPaidSubscriptionAccesses) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_ACCESSES,
                        new Object[] {
                                localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES, locale)
                        },
                        locale));
            }

            if (withTrialSubscriptionAccesses) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_TRIAL_SUBSCRIPTION_ACCESSES,
                        new Object[] {
                                MemoryUtils.humanReadableByteCount(paidSubscriptionProperties.getTrialMaxFileSize()),
                                paidSubscriptionProperties.getTrialMaxActionsCount()
                        },
                        locale));
            }

            if (withCheckSubscriptionCommand) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(
                        localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_CHECK_COMMAND, locale)
                );
            }

            if (withSubscriptionInstructions) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(
                        localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_INSTRUCTION,
                                new Object[]{paidSubscriptionProperties.getPaymentBotName()}, locale)
                );
            }

            if (withRenewInstructions) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(
                        localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_RENEW, locale)
                );
            }

            if (withPaidSubscriptionFeatures) {
                if (!message.toString().endsWith("\n\n")) {
                    message.append("\n\n");
                }
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_BUY_SUBSCRIPTION_RIGHT_NOW,
                        new Object[]{
                                NumberUtils.toString(minPrice, 2)
                        }, locale)).append("\n");
                message.append(localisationService.getMessage(MessagesProperties.MESSAGE_PAID_SUBSCRIPTION_FEATURES,
                        locale));
            }

            return message.toString();
        }
    }
}
