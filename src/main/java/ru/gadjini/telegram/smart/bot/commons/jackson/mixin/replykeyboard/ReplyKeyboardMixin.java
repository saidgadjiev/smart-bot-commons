package ru.gadjini.telegram.smart.bot.commons.jackson.mixin.replykeyboard;

import com.fasterxml.jackson.databind.annotation.JsonAppend;

@JsonAppend(
        props = {
                @JsonAppend.Prop(value = ReplyKeyboardWriter.class, name = "class", type = Class.class)
        }
)
public class ReplyKeyboardMixin {
}
