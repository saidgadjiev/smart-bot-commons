package ru.gadjini.telegram.smart.bot.commons.redis;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CustomRedisListOps {

    private RedisTemplate<String, Object> redisTemplate;

    public CustomRedisListOps(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<String> leftPop(String key, long count) {
        return redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            LettuceConnection nativeConnection = (LettuceConnection) connection.getNativeConnection();
            RedisClusterAsyncCommands<byte[], byte[]> command = nativeConnection.getNativeConnection();

            RedisFuture<List<byte[]>> lPop = command.lpop(rawKey(key), count);

            try {
                return CollectionUtils.isEmpty(lPop.get()) ? null : deserializeValue(lPop.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private byte[] rawKey(String key) {
        Assert.notNull(key, "non null key required");

        return keySerializer().serialize(key);
    }

    private RedisSerializer<String> keySerializer() {
        return RedisSerializer.string();
    }

    private List<String> deserializeValue(List<byte[]> value) {
        return value.stream().map(v -> valueSerializer().deserialize(v)).collect(Collectors.toList());
    }

    private RedisSerializer<String> valueSerializer() {
        return RedisSerializer.string();
    }
}
