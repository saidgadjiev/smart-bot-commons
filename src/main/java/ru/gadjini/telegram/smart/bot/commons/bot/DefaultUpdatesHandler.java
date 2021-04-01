package ru.gadjini.telegram.smart.bot.commons.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandExecutor;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandsContainer;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardHolderService;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.utils.MessageUtils;

import java.util.Collections;

@Component
public class DefaultUpdatesHandler implements UpdatesHandler {

    private MessageService messageService;

    private CommandExecutor commandExecutor;

    private LocalisationService localisationService;

    private UserService userService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandsContainer commandsContainer;

    @Autowired
    public DefaultUpdatesHandler(@TgMessageLimitsControl MessageService messageService,
                                 LocalisationService localisationService, UserService userService,
                                 @KeyboardHolder ReplyKeyboardService replyKeyboardService) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.replyKeyboardService = replyKeyboardService;
    }

    @Autowired
    public void setCommandsContainer(CommandsContainer commandsContainer) {
        this.commandsContainer = commandsContainer;
    }

    @Autowired
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String text = MessageUtils.getText(update.getMessage());
            Message message = update.getMessage();
            if (message.hasSuccessfulPayment()) {
                commandExecutor.processSuccessfulPayment(message);
            } else {
                if (commandsContainer.isKeyboardCommand(update.getMessage().getChatId(), text)) {
                    if (isOnCurrentMenu(update.getMessage().getChatId(), text)) {
                        commandExecutor.executeKeyBoardCommand(update.getMessage(), text);

                        return;
                    }
                } else if (commandsContainer.isBotCommand(update.getMessage())) {
                    if (commandExecutor.executeBotCommand(update.getMessage())) {
                        return;
                    } else {
                        messageService.sendMessage(
                                SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                        .text(localisationService.getMessage(MessagesProperties.MESSAGE_UNKNOWN_COMMAND,
                                                userService.getLocaleOrDefault(update.getMessage().getFrom().getId())))
                                        .parseMode(ParseMode.HTML)
                                        .build());
                        return;
                    }
                }
                commandExecutor.processNonCommandUpdate(update.getMessage(), text);
            }
        } else if (update.hasCallbackQuery()) {
            commandExecutor.executeCallbackCommand(update.getCallbackQuery());
        } else if (update.hasPreCheckoutQuery()) {
            commandExecutor.processPreCheckoutQuery(update.getPreCheckoutQuery());
        }
    }

    private boolean isOnCurrentMenu(long chatId, String commandText) {
        ReplyKeyboardMarkup replyKeyboardMarkup = ((ReplyKeyboardHolderService) replyKeyboardService).getCurrentReplyKeyboard(chatId);

        if (replyKeyboardMarkup == null) {
            return true;
        }
        if (replyKeyboardMarkup.getKeyboard() == null) {
            replyKeyboardMarkup.setKeyboard(Collections.emptyList());
        }

        for (KeyboardRow keyboardRow : replyKeyboardMarkup.getKeyboard()) {
            if (keyboardRow.stream().anyMatch(keyboardButton -> keyboardButton.getText().equals(commandText))) {
                return true;
            }
        }

        return false;
    }
}
