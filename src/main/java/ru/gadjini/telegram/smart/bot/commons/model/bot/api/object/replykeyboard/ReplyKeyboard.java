package ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.gadjini.telegram.smart.bot.commons.model.bot.api.object.replykeyboard.serialization.KeyboardDeserializer;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * Reply keyboard abstract type
 */
@JsonDeserialize(using = KeyboardDeserializer.class)
public interface ReplyKeyboard {
}
