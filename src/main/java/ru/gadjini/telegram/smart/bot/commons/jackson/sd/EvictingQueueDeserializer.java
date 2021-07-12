package ru.gadjini.telegram.smart.bot.commons.jackson.sd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.EvictingQueue;

import java.io.IOException;

public class EvictingQueueDeserializer extends StdDeserializer<EvictingQueue> {

    private ObjectMapper objectMapper;

    public EvictingQueueDeserializer(ObjectMapper objectMapper) {
        this((Class) null);
        this.objectMapper = objectMapper;
    }

    protected EvictingQueueDeserializer(Class<EvictingQueue> vc) {
        super(vc);
    }

    @Override
    public EvictingQueue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode treeNode = p.getCodec().readTree(p);
        int size = treeNode.get("size").asInt();
        EvictingQueue evictingQueue = EvictingQueue.create(size);

        if (treeNode.has("itemClass")) {
            String itemClass = treeNode.get("itemClass").asText();
            ArrayNode node = (ArrayNode) treeNode.get("queue");
            Class<?> itemCl;
            try {
                itemCl = Class.forName(itemClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            for (JsonNode jsonNode : node) {
                Object o = objectMapper.convertValue(jsonNode, itemCl);
                evictingQueue.add(o);
            }
        }

        return evictingQueue;
    }
}
