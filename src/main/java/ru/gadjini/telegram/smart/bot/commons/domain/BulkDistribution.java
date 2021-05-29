package ru.gadjini.telegram.smart.bot.commons.domain;

import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;

import java.util.Locale;

public class BulkDistribution {

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String MESSAGE_RU = "message_ru";

    public static final String MESSAGE_EN = "message_en";

    public static final String MESSAGE_UZ = "message_uz";

    private int id;

    private int userId;

    private String messageRu;

    private String messageEn;

    private String messageUz;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessageRu() {
        return messageRu;
    }

    public void setMessageRu(String messageRu) {
        this.messageRu = messageRu;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public void setMessageEn(String messageEn) {
        this.messageEn = messageEn;
    }

    public String getMessageUz() {
        return messageUz;
    }

    public void setMessageUz(String messageUz) {
        this.messageUz = messageUz;
    }

    public String getMessage(Locale locale) {
        switch (locale.getLanguage()) {
            case LocalisationService
                    .RU_LOCALE:
                return getMessageRu();
            case LocalisationService.UZ_LOCALE:
                return getMessageUz();
            default:
                return getMessageEn();
        }
    }
}
