package ru.gadjini.telegram.smart.bot.commons.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.EvictingQueue;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.jackson.mixin.replykeyboard.ReplyKeyboardMixin;
import ru.gadjini.telegram.smart.bot.commons.jackson.sd.EvictingQueueDeserializer;
import ru.gadjini.telegram.smart.bot.commons.jackson.sd.EvictingQueueSerializer;

import java.lang.annotation.Annotation;

@Configuration
public class JacksonConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(EvictingQueue.class, new EvictingQueueSerializer());
        simpleModule.addDeserializer(EvictingQueue.class, new EvictingQueueDeserializer(new ObjectMapper()));
        jacksonObjectMapperBuilder
                .modules(new JavaTimeModule(), new JodaModule())
                .annotationIntrospector(new JacksonAnnotationIntrospector() {
                    @Override
                    protected <A extends Annotation> A _findAnnotation(final Annotated annotated,
                                                                       final Class<A> annoClass) {
                        if (!annotated.hasAnnotation(JsonIgnore.class)
                                && !annotated.hasAnnotation(JsonSerialize.class)) {
                            return super._findAnnotation(annotated, annoClass);
                        }
                        return null;
                    }
                })
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .mixIn(ReplyKeyboard.class, ReplyKeyboardMixin.class)
                .failOnUnknownProperties(false);
    }
}
