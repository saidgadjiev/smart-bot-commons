package ru.gadjini.telegram.smart.bot.commons.model;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;

import java.util.ArrayList;
import java.util.List;

public class TgMessage {

    private long chatId;

    private User user;

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public User getUser() {
        return user;
    }

    public static TgMessage from(CallbackQuery callbackQuery) {
        TgMessage tgMessage = new TgMessage();

        tgMessage.chatId = callbackQuery.getMessage().getChatId();
        tgMessage.user = callbackQuery.getFrom();

        return tgMessage;
    }

    public static TgMessage from(PreCheckoutQuery preCheckoutQuery) {
        TgMessage tgMessage = new TgMessage();

        tgMessage.chatId = preCheckoutQuery.getFrom().getId();
        tgMessage.user = preCheckoutQuery.getFrom();

        return tgMessage;
    }

    public static TgMessage from(Message message) {
        TgMessage tgMessage = new TgMessage();

        tgMessage.chatId = message.getChatId();
        tgMessage.user = message.getFrom();

        return tgMessage;
    }

    public static TgMessage from(Update update) {
        if (update.hasCallbackQuery()) {
            return from(update.getCallbackQuery());
        } else if (update.hasPreCheckoutQuery()) {
            return from(update.getPreCheckoutQuery());
        }

        return from(update.getMessage());
    }

    public static long getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom().getId();
        }

        return update.getMessage().getChatId();
    }

    public static int getUserId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom().getId();
        }

        return update.getMessage().getFrom().getId();
    }

    public static User getUser(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom();
        }

        return update.getMessage().getFrom();
    }

    public static List<MetaType> getMetaTypes(Message message) {
        List<MetaType> metaTypes = new ArrayList<>();

        if (message.hasDocument()) {
            metaTypes.add(MetaType.DOCUMENT);
        }
        if (message.hasText()) {
            metaTypes.add(MetaType.TEXT);
        }
        if (message.hasPhoto()) {
            metaTypes.add(MetaType.PHOTO);
        }
        if (message.hasVideo()) {
            metaTypes.add(MetaType.VIDEO);
        }
        if (message.hasAudio()) {
            metaTypes.add(MetaType.AUDIO);
        }

        return metaTypes;
    }

    @Override
    public String toString() {
        return "TgMessage{" +
                "chatId=" + chatId +
                ", user=" + user +
                '}';
    }

    public enum MetaType {

        TEXT,

        DOCUMENT,

        AUDIO,

        VIDEO,

        PHOTO,

        VOICE,

        CONTACT,

        LOCATION
    }
}
