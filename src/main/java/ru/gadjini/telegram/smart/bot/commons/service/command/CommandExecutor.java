package ru.gadjini.telegram.smart.bot.commons.service.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.CallbackBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.KeyboardBotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;

@Service
public class CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    private CommandParser commandParser;

    private CommandNavigator commandNavigator;

    private CommandsContainer commandsContainer;

    @Autowired
    public CommandExecutor(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Autowired
    public void setCommandsContainer(CommandsContainer commandsContainer) {
        this.commandsContainer = commandsContainer;
    }

    @Autowired
    public void setCommandNavigator(CommandNavigator commandNavigator) {
        this.commandNavigator = commandNavigator;
    }

    public void cancelCommand(long chatId, String queryId) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(chatId);

        if (navigableBotCommand != null) {
            navigableBotCommand.cancel(chatId, queryId);
        }
    }

    public void processNonCommandUpdate(Message message, String text) {
        NavigableBotCommand navigableBotCommand = commandNavigator.getCurrentCommand(message.getChatId());

        if (navigableBotCommand != null && navigableBotCommand.acceptNonCommandMessage(message)) {
            navigableBotCommand.processNonCommandUpdate(message, text);
        }
    }

    public void processPreCheckoutQuery(PreCheckoutQuery preCheckoutQuery) {
        commandsContainer.getPaymentsHandler().preCheckout(preCheckoutQuery);
    }

    public void processSuccessfulPayment(Message message) {
        commandsContainer.getPaymentsHandler().successfulPayment(message);
    }

    public boolean executeBotCommand(Message message) {
        CommandParser.CommandParseResult commandParseResult = commandParser.parseBotCommand(message);
        BotCommand botCommand = commandsContainer.getBotCommand(commandParseResult.getCommandName());

        if (botCommand != null) {
            LOGGER.debug("Bot({}, {})", message.getFrom().getId(), botCommand.getClass().getSimpleName());
            if (botCommand.accept(message)) {
                botCommand.processMessage(message, commandParseResult.getParameters());

                if (botCommand instanceof NavigableBotCommand) {
                    commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
                }

                return true;
            }
        }

        return false;
    }

    public void executeKeyBoardCommand(Message message, String text) {
        KeyboardBotCommand botCommand = commandsContainer.getKeyboardBotCommand(message.getChatId(), text);

        LOGGER.debug("Keyboard({}, {})", message.getFrom().getId(), botCommand.getClass().getSimpleName());
        boolean pushToHistory = botCommand.processMessage(message, message.getText());

        if (pushToHistory) {
            commandNavigator.push(message.getChatId(), (NavigableBotCommand) botCommand);
        }
    }

    public void executeCallbackCommand(CallbackQuery callbackQuery) {
        CommandParser.CommandParseResult parseResult = commandParser.parseCallbackCommand(callbackQuery);
        CallbackBotCommand botCommand = commandsContainer.getCallbackBotCommand(parseResult.getCommandName());

        LOGGER.debug("Callback({}, {})", callbackQuery.getFrom().getId(), botCommand.getClass().getSimpleName());
        botCommand.processCallbackQuery(callbackQuery, parseResult.getRequestParams());
    }
}
