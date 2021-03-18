package ru.gadjini.telegram.smart.bot.commons.service;

import java.util.Locale;

public class DummyCommandMessageBuilder implements CommandMessageBuilder {
    @Override
    public String getCommandsInfo(Locale locale) {
        return "I'm dummy i have no commands";
    }
}
