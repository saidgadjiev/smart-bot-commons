package ru.gadjini.telegram.smart.bot.commons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Jackson {

    private ObjectMapper objectMapper;

    @Autowired
    public Jackson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public<T> T convertValue(Object o, Class<T> clazz) {
        if (o == null) {
            return null;
        }
        return objectMapper.convertValue(o, clazz);
    }

    public<T> T convertValue(Object o, TypeReference<T> toValueTypeRef) {
        if (o == null) {
            return null;
        }
        return objectMapper.convertValue(o, toValueTypeRef);
    }

    public<T> T readValue(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public<T> T readValue(String json, TypeReference<T> toValueTypeRef) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, toValueTypeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String writeValueAsString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
