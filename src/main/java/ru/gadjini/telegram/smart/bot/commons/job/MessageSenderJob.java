package ru.gadjini.telegram.smart.bot.commons.job;

import com.antkorwin.xsync.XSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.gadjini.telegram.smart.bot.commons.exception.FloodWaitException;
import ru.gadjini.telegram.smart.bot.commons.property.MessagesSenderJobProperties;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.message.queue.MessageItem;
import ru.gadjini.telegram.smart.bot.commons.service.message.queue.MessagesQueue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MessageSenderJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSenderJob.class);

    private MessageService messageService;

    private MessagesQueue messagesQueue;

    private MessagesSenderJobProperties messagesSenderJobProperties;

    private XSync<String> messagesQueueXSync;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    public MessageSenderJob(MessagesQueue messagesQueue,
                            @Qualifier("message") MessageService messageService,
                            MessagesSenderJobProperties messagesSenderJobProperties,
                            @Qualifier("messagesQueue") XSync<String> messagesQueueXSync) {
        this.messagesQueue = messagesQueue;
        this.messageService = messageService;
        this.messagesSenderJobProperties = messagesSenderJobProperties;
        this.messagesQueueXSync = messagesQueueXSync;

        LOGGER.debug("Message sender job initialized");
    }

    @Scheduled(fixedDelay = 30)
    public void send() {
        if (messagesSenderJobProperties.isDisable()) {
            return;
        }
        List<String> recipients = messagesQueue.getRecipients(30);

        for (String recipient : recipients) {
            executorService.execute(() -> {
                if (messagesQueue.isExistsFloodProtectingKey(recipient)) {
                    return;
                }
                MessageItem message = getNextMessage(recipient);
                if (message == null) {
                    return;
                }
                long between = ChronoUnit.SECONDS.between(message.getCreatedAt(), LocalDateTime.now());

                if (between > 1) {
                    LOGGER.debug("Send message latency({}, {})", between, message.getMessage());
                }
                try {
                    sendMessage(message);
                    messagesQueue.popMessage(recipient);
                } catch (FloodWaitException e) {
                    LOGGER.error(e.getMessage());
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }

                MessageItem nextMessage = getNextMessage(recipient);
                if (nextMessage != null) {
                    long floodProtectingTimeInMillis = getFloodProtectingTimeInMillis(nextMessage);
                    messagesQueue.createFloodProtectingKey(recipient, floodProtectingTimeInMillis);
                    messagesQueue.pushRecipientToTheEndOfQueue(recipient);
                } else {
                    messagesQueue.createFloodProtectingKey(recipient, 1000);
                }
            });
        }
    }

    private long getFloodProtectingTimeInMillis(MessageItem messageItem) {
        switch (messageItem.getPath()) {
            case SendMessage.PATH:
            case SendInvoice.PATH:
                return 1000;
            case EditMessageText.PATH:
            case EditMessageReplyMarkup.PATH:
            case EditMessageCaption.PATH:
                return 500;
            default:
                return 0;
        }
    }

    private void sendMessage(MessageItem messageItem) {
        switch (messageItem.getPath()) {
            case SendMessage.PATH:
                messageService.sendMessage((SendMessage) messageItem.getMessage());
                break;
            case EditMessageText.PATH:
                messageService.editMessage((EditMessageText) messageItem.getMessage());
                break;
            case EditMessageReplyMarkup.PATH:
                messageService.editKeyboard((EditMessageReplyMarkup) messageItem.getMessage());
                break;
            case EditMessageCaption.PATH:
                messageService.editMessageCaption((EditMessageCaption) messageItem.getMessage());
                break;
            case SendInvoice.PATH:
                messageService.sendInvoice((SendInvoice) messageItem.getMessage());
                break;
        }
    }

    private MessageItem getNextMessage(String recipient) {
        AtomicReference<MessageItem> message = new AtomicReference<>(messagesQueue.getMessage(recipient));
        if (message.get() == null) {
            messagesQueueXSync.execute(recipient, () -> {
                message.set(messagesQueue.getMessage(recipient));
                if (message.get() == null) {
                    messagesQueue.removeRecipient(recipient);
                }
            });
            if (message.get() == null) {
                return null;
            }
        }

        return message.get();
    }
}