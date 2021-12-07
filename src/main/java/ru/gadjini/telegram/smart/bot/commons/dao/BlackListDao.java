package ru.gadjini.telegram.smart.bot.commons.dao;

public interface BlackListDao {
    Boolean isInBlackList(long userId);
}
