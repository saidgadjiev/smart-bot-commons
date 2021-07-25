package ru.gadjini.telegram.smart.bot.commons.service.command.navigator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.annotation.Redis;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.dao.command.navigator.keyboard.CommandNavigatorDao;
import ru.gadjini.telegram.smart.bot.commons.exception.UserException;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class CommandNavigator {

    private Map<String, NavigableBotCommand> navigableBotCommands = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandNavigator.class);

    private CommandNavigatorDao navigatorDao;

    private LocalisationService localisationService;

    private UserService userService;

    @Autowired
    public CommandNavigator(@Redis CommandNavigatorDao navigatorDao,
                            LocalisationService localisationService, UserService userService) {
        this.navigatorDao = navigatorDao;
        this.localisationService = localisationService;
        this.userService = userService;
    }

    @Autowired
    public void setNavigableCommands(Collection<NavigableBotCommand> navigableBotCommands) {
        navigableBotCommands.forEach(n -> this.navigableBotCommands.put(n.getHistoryName(), n));
    }

    public void push(long chatId, NavigableBotCommand navigableBotCommand) {
        NavigableBotCommand currCommand = getCurrentCommand(chatId, false);

        if (currCommand != null) {
            if (Objects.equals(currCommand.getHistoryName(), navigableBotCommand.getHistoryName())) {
                return;
            }
            if (!navigableBotCommand.setPrevCommand(chatId, currCommand.getHistoryName())) {
                currCommand.leave(chatId);
            }
        }

        setCurrentCommand(chatId, navigableBotCommand);
    }

    public boolean isEmpty(long chatId) {
        return navigatorDao.get(chatId) == null;
    }

    public void pop(TgMessage message) {
        long chatId = message.getChatId();
        NavigableBotCommand currentCommand = getCurrentCommand(chatId, false);
        if (currentCommand == null) {
            NavigableBotCommand parentCommand = navigableBotCommands.get(CommandNames.START_COMMAND_NAME);

            setCurrentCommand(chatId, parentCommand);
            parentCommand.restore(message);
        } else {
            if (currentCommand.canLeave(chatId)) {
                String parentHistoryName = currentCommand.getParentCommandName(chatId);

                if (StringUtils.isNotBlank(parentHistoryName)) {
                    currentCommand.leave(chatId);

                    NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);
                    setCurrentCommand(chatId, parentCommand);
                    parentCommand.restore(message);
                } else {
                    currentCommand.restore(message);
                }
            } else {
                currentCommand.restore(message);
            }
        }
    }

    public SilentPop silentPop(long chatId) {
        NavigableBotCommand navigableBotCommand = getCurrentCommand(chatId, false);
        if (navigableBotCommand == null) {
            return null;
        }
        String parentHistoryName = navigableBotCommand.getParentCommandName(chatId);

        navigableBotCommand.leave(chatId);
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        setCurrentCommand(chatId, parentCommand);

        return new SilentPop(parentCommand.getKeyboard(chatId), parentCommand.getMessage(chatId));
    }

    public void silentPopFromCallback(long chatId) {
        NavigableBotCommand navigableBotCommand = getCurrentCommand(chatId, false);
        if (navigableBotCommand == null) {
            return;
        }
        String parentHistoryName = navigableBotCommand.getParentCommandName(chatId);

        navigableBotCommand.leave(chatId);
        NavigableBotCommand parentCommand = navigableBotCommands.get(parentHistoryName);

        setCurrentCommand(chatId, parentCommand);
    }

    public NavigableBotCommand getCurrentCommand(long chatId, boolean throwRestartedException) {
        String currCommand = navigatorDao.get(chatId);

        if (currCommand == null) {
            LOGGER.debug("Bot restarted({})", chatId);
            zeroRestore(chatId, navigableBotCommands.get(CommandNames.START_COMMAND_NAME));
            currCommand = CommandNames.START_COMMAND_NAME;
            if (throwRestartedException) {
                throw new UserException(localisationService.getMessage(MessagesProperties.MESSAGE_BOT_RESTARTED,
                        userService.getLocaleOrDefault((int) chatId)));
            }
        }

        return navigableBotCommands.get(currCommand);
    }

    public void setCurrentCommand(long chatId, String command) {
        navigatorDao.set(chatId, command);
    }

    public String getCurrentCommandName(long chatId) {
        return navigatorDao.get(chatId);
    }

    private void setCurrentCommand(long chatId, NavigableBotCommand navigableBotCommand) {
        navigatorDao.set(chatId, navigableBotCommand.getHistoryName());
    }

    private void zeroRestore(long chatId, NavigableBotCommand botCommand) {
        setCurrentCommand(chatId, botCommand);
    }

    public static class SilentPop {

        private ReplyKeyboard replyKeyboardMarkup;

        private String message;

        private SilentPop(ReplyKeyboard replyKeyboard, String message) {
            this.replyKeyboardMarkup = replyKeyboard;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public ReplyKeyboard getReplyKeyboardMarkup() {
            return replyKeyboardMarkup;
        }
    }
}
