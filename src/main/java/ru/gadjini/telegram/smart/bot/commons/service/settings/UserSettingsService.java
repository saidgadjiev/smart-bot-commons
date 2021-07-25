package ru.gadjini.telegram.smart.bot.commons.service.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.dao.UserSettingsDao;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;

@Service
@SuppressWarnings("PMD")
public class UserSettingsService {

    private UserSettingsDao userSettingsDao;

    private BotProperties botProperties;

    @Autowired
    public UserSettingsService(UserSettingsDao userSettingsDao, BotProperties botProperties) {
        this.userSettingsDao = userSettingsDao;
        this.botProperties = botProperties;
    }

    public void createDefaultSettings(long userId) {
        userSettingsDao.createDefaultSettings(botProperties.getName(), userId);
    }

    public void smartFileFeature(long userId, boolean enable) {
        userSettingsDao.smartFileFeature(botProperties.getName(), userId, enable);
    }

    public boolean isSmartFileFeatureEnabled(long userId) {
        return userSettingsDao.getSmartFileFeatureEnabledOrDefault(botProperties.getName(), userId);
    }
}
