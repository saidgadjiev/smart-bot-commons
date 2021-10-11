package ru.gadjini.telegram.smart.bot.commons.service.message;

public class MessageEvent {

    private Object event;

    private Object sendResult;

    public MessageEvent(Object event, Object sendResult) {
        this.event = event;
        this.sendResult = sendResult;
    }

    public Object getEvent() {
        return event;
    }

    public Object getSendResult() {
        return sendResult;
    }
}
