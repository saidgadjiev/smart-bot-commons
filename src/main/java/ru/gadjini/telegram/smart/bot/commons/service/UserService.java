package ru.gadjini.telegram.smart.bot.commons.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.dao.UserDao;
import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.domain.TgUser;
import ru.gadjini.telegram.smart.bot.commons.exception.botapi.TelegramApiRequestException;
import ru.gadjini.telegram.smart.bot.commons.service.settings.UserSettingsService;

import java.util.Locale;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private UserDao userDao;

    private LocalisationService localisationService;

    private UserSettingsService userSettingsService;

    @Autowired
    public UserService(@Redis UserDao userDao, LocalisationService localisationService,
                       UserSettingsService userSettingsService) {
        this.userDao = userDao;
        this.localisationService = localisationService;
        this.userSettingsService = userSettingsService;
    }

    public CreateOrUpdateResult createOrUpdate(User user) {
        TgUser tgUser = new TgUser();
        tgUser.setUserId(user.getId());
        tgUser.setUsername(user.getUserName());
        tgUser.setOriginalLocale(user.getLanguageCode());

        String language = localisationService.getSupportedLocales().stream()
                .filter(locale -> locale.getLanguage().equals(user.getLanguageCode()))
                .findAny().orElse(Locale.getDefault()).getLanguage();
        tgUser.setLocale(language);
        CreateOrUpdateResult.State state = userDao.createOrUpdate(tgUser);

        CreateOrUpdateResult createOrUpdateResult = new CreateOrUpdateResult(tgUser, state);

        if (createOrUpdateResult.isCreated()) {
            userSettingsService.createDefaultSettings(user.getId());
        }

        return createOrUpdateResult;
    }

    public Locale getLocaleOrDefault(int userId) {
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
            createOrUpdate(user);
            LOGGER.debug("User created({})", user.getId());
        }
    }

    public void blockUser(int userId) {
        userDao.blockUser(userId);
    }

    public boolean handleBotBlockedByUser(Throwable ex) {
        int apiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);

        if (apiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException exception = (TelegramApiRequestException) ExceptionUtils.getThrowableList(ex).get(apiRequestExceptionIndexOf);
            if (exception.getErrorCode() == 403) {
                blockUser(Integer.parseInt(exception.getChatId()));

                return true;
            }
        }

        return false;
    }

    public boolean isAdmin(int userId) {
        return userId == 171271164;
    }

    public void changeLocale(int userId, Locale locale) {
        userDao.updateLocale(userId, locale.getLanguage());
    }
}
