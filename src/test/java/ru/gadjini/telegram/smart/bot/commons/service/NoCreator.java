package ru.gadjini.telegram.smart.bot.commons.service;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NoCreator {

    private String name;

    @JsonIgnore
    private String ignore;

    public NoCreator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIgnore() {
        return ignore;
    }

    public void setIgnore(String ignore) {
        this.ignore = ignore;
    }
}
