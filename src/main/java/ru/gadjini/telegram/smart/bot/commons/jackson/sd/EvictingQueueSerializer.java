package ru.gadjini.telegram.smart.bot.commons.jackson.sd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.EvictingQueue;

import java.io.IOException;

public class EvictingQueueSerializer extends StdSerializer<EvictingQueue> {

    public EvictingQueueSerializer() {
        this(null);
    }


    protected EvictingQueueSerializer(Class<EvictingQueue> t) {
        super(t);
    }

    @Override
    public void serialize(EvictingQueue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("size", value.size() + value.remainingCapacity());
        if (!value.isEmpty()) {
            gen.writeStringField("itemClass", value.iterator().next().getClass().getName());
        }
        gen.writeArrayFieldStart("queue");
        for (Object o : value) {
            gen.writeObject(o);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
