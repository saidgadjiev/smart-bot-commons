package ru.gadjini.telegram.smart.bot.commons.service.message.queue;

public class MessageItem {

    private String path;

    private Object message;

    public MessageItem() {

    }

    public MessageItem(String path, Object message) {
        this.path = path;
        this.message = message;
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
}
