package ru.gadjini.telegram.smart.bot.commons.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.Update;

@Service
public class TgToSmartModelMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public TgToSmartModelMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Update map(org.telegram.telegrambots.meta.api.objects.Update update) {
        return objectMapper.convertValue(update, Update.class);
    }
}
