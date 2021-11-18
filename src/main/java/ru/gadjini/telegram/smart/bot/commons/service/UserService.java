package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.UserDao;
import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;
import ru.gadjini.telegram.smart.bot.commons.event.UserBlockedEvent;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.service.settings.UserSettingsService;

import java.util.Locale;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private UserDao userDao;

    private LocalisationService localisationService;

    private UserSettingsService userSettingsService;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public UserService(@Redis UserDao userDao, LocalisationService localisationService,
                       UserSettingsService userSettingsService, ApplicationEventPublisher applicationEventPublisher) {
        this.userDao = userDao;
        this.localisationService = localisationService;
        this.userSettingsService = userSettingsService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void createOrUpdate(User user, String startParameter) {
        TgUser tgUser = new TgUser();
        tgUser.setUserId(user.getId());
        tgUser.setUsername(user.getUserName());
        tgUser.setOriginalLocale(user.getLanguageCode());
        tgUser.setStartParameter(startParameter);

        String language = localisationService.getSupportedLocales().stream()
                .filter(locale -> locale.getLanguage().equals(user.getLanguageCode()))
                .findAny().orElse(Locale.getDefault()).getLanguage();
        tgUser.setLocale(language);
        CreateOrUpdateResult.State state = userDao.createOrUpdate(tgUser);

        CreateOrUpdateResult createOrUpdateResult = new CreateOrUpdateResult(tgUser, state);

        if (createOrUpdateResult.isCreated()) {
            userSettingsService.createDefaultSettings(user.getId());
        }
    }

    public Locale getLocaleOrDefault(long userId) {
        String locale = userDao.getLocale(userId);

        if (StringUtils.isNotBlank(locale)) {
            return new Locale(locale);
        }

        return Locale.getDefault();
    }

    public long countActiveUsers(int intervalInDays) {
        return userDao.countActiveUsers(intervalInDays);
    }

    public void activity(User user) {
        if (user == null) {
            LOGGER.error("User is null");
            return;
        }
        int updated = userDao.updateActivity(user.getId(), user.getUserName());

        if (updated == 0) {
            createOrUpdate(user, null);
            LOGGER.debug("User created({})", user.getId());
        }
    }

    public void blockUser(long userId) {
        userDao.blockUser(userId);
    }

    public boolean handleBotBlockedByUser(Throwable ex) {
        int apiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);

        if (apiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException exception = (TelegramApiRequestException) ExceptionUtils.getThrowableList(ex).get(apiRequestExceptionIndexOf);
            if (exception.getErrorCode() == 403) {
                long userId = Long.parseLong(exception.getChatId());
                blockUser(userId);
                applicationEventPublisher.publishEvent(new UserBlockedEvent(userId));

                return true;
            }
        }

        return false;
    }

    public boolean isAdmin(long userId) {
        return userId == 171271164;
    }

    public void changeLocale(long userId, Locale locale) {
        userDao.updateLocale(userId, locale.getLanguage());
    }
}
