package ru.gadjini.telegram.smart.bot.commons.service.message.queue;

import java.time.LocalDateTime;

public class MessageItem {

    private String path;

    private Object message;

    private Object event;

    private Class<?> eventClass;

    private LocalDateTime createdAt;

    public MessageItem() {

    }

    public MessageItem(String path, Object message) {
        this.path = path;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public MessageItem(String path, Object message, Object event, Class<?> eventClass) {
        this(path, message);
        this.event = event;
        this.eventClass = eventClass;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Object getEvent() {
        return event;
    }

    public Class<?> getEventClass() {
        return eventClass;
    }

    public void setEvent(Object event) {
        this.event = event;
    }
}
