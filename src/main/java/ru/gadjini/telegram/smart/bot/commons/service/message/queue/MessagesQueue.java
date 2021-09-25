package ru.gadjini.telegram.smart.bot.commons.service.message.queue;

import com.antkorwin.xsync.XSync;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class MessagesQueue {

    private static final String RECIPIENTS_KEY = "recipients";

    private static final String MESSAGES_KEY = "messages";

    private static final String FPROTECT_KEY = "fprotect";

    private ObjectMapper objectMapper;

    private RedisTemplate<String, Object> redisTemplate;

    private XSync<String> messagesQueueXSync;

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public MessagesQueue(ObjectMapper objectMapper, RedisTemplate<String, Object> redisTemplate,
                         @Qualifier("messagesQueue") XSync<String> messagesQueueXSync,
                         StringRedisTemplate stringRedisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.messagesQueueXSync = messagesQueueXSync;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public List<String> getRecipients(long count) {
        return stringRedisTemplate.opsForList().range(RECIPIENTS_KEY, -1 * count, -1);
    }

    public void removeRecipient(String chatId) {
        messagesQueueXSync.execute(chatId, () -> stringRedisTemplate.opsForList().remove(RECIPIENTS_KEY, 1, chatId));
    }

    public void pushRecipientToTheEndOfQueue(String chatId) {
        messagesQueueXSync.execute(chatId, () -> {
            stringRedisTemplate.opsForList().remove(RECIPIENTS_KEY, 1, chatId);
            stringRedisTemplate.opsForList().leftPush(RECIPIENTS_KEY, chatId);
        });
    }

    public void popMessage(String chatId) {
        messagesQueueXSync.execute(chatId, () -> {
            String messagesKey = getMessagesKey(chatId);
            redisTemplate.opsForList().rightPop(messagesKey);
        });
    }

    public void createFloodProtectingKey(String chatId, long expireInMillis) {
        String key = getFprotectKey(chatId);
        redisTemplate.opsForValue().set(key, true);
        redisTemplate.expire(key, expireInMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isExistsFloodProtectingKey(String chatId) {
        return BooleanUtils.isTrue(redisTemplate.hasKey(getFprotectKey(chatId)));
    }

    public MessageItem getMessage(String chatId) {
        return messagesQueueXSync.evaluate(chatId, () -> {
            String messagesKey = getMessagesKey(chatId);

            List<Object> range = redisTemplate.opsForList().range(messagesKey, -1, -1);

            List<MessageItem> messages = range == null ? List.of() : ((List<Map<String, Object>>) (Object) range).stream()
                    .map(this::map).collect(Collectors.toList());

            return messages.isEmpty() ? null : messages.get(0);
        });
    }

    public void add(SendMessage sendMessage) {
        messagesQueueXSync.execute(sendMessage.getChatId(), () -> {
            String messagesKey = getMessagesKey(sendMessage.getChatId());
            redisTemplate.opsForList().leftPush(messagesKey, new MessageItem(SendMessage.PATH, sendMessage));
            pushRecipient(sendMessage.getChatId());
        });
    }

    private void pushRecipient(String chatId) {
        Long aLong = stringRedisTemplate.opsForList().indexOf(RECIPIENTS_KEY, chatId);
        if (aLong == null) {
            stringRedisTemplate.opsForList().leftPush(RECIPIENTS_KEY, chatId);
        }
    }

    private String getFprotectKey(String chatId) {
        return FPROTECT_KEY + ":" + chatId;
    }

    private String getMessagesKey(String chatId) {
        return MESSAGES_KEY + ":" + chatId;
    }

    public MessageItem map(Map<String, Object> values) {
        MessageItem messageItem = new MessageItem();
        messageItem.setPath((String) values.get("path"));

        switch (messageItem.getPath()) {
            case SendMessage.PATH:
                messageItem.setMessage(objectMapper.convertValue(values.get("message"), SendMessage.class));
        }

        return messageItem;
    }
}
