package ru.gadjini.telegram.smart.bot.commons.service.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParamsParser;

import java.util.Arrays;

@Service
public class CommandParser {

    public static final String COMMAND_ARG_SEPARATOR = "=";

    public static final String BOT_COMMAND_ARG_SEPARATOR = "_";

    public static final String START_PARAMETER_SEPARATOR = " ";

    public static final String COMMAND_NAME_SEPARATOR = ":";

    public static final String COMMAND_START_CHAR = "/";

    private RequestParamsParser requestParamsParser;

    @Autowired
    public CommandParser(RequestParamsParser requestParamsParser) {
        this.requestParamsParser = requestParamsParser;
    }

    public CommandParseResult parseCallbackCommand(CallbackQuery callbackQuery) {
        String text = callbackQuery.getData();
        String[] commandSplit = text.split(COMMAND_NAME_SEPARATOR);
        RequestParams requestParams = new RequestParams();

        if (commandSplit.length > 1) {
            requestParams = requestParamsParser.parse(commandSplit[1]);
        }

        return new CommandParseResult(commandSplit[0], requestParams);
    }

    public CommandParseResult parseBotCommand(Message message) {
        String text = message.getText().trim();

        if (isStartCommand(text)) {
            return parseStartCommand(text);
        } else {
            return parseBotCommand(text);
        }
    }

    private CommandParseResult parseBotCommand(String text) {
        String[] commandSplit = text.split(COMMAND_ARG_SEPARATOR);
        if (commandSplit.length < 2) {
            commandSplit = text.split(BOT_COMMAND_ARG_SEPARATOR);
        }
        String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);

        return new CommandParseResult(commandSplit[0].substring(1), parameters);
    }

    private CommandParseResult parseStartCommand(String text) {
        String[] commandSplit = text.split(START_PARAMETER_SEPARATOR);

        return new CommandParseResult(commandSplit[0].substring(1), commandSplit.length > 1 ? commandSplit[1] : null);
    }

    private boolean isStartCommand(String text) {
        return text.startsWith(COMMAND_START_CHAR + CommandNames.START_COMMAND_NAME);
    }

    public static class CommandParseResult {

        private String commandName;

        private String[] parameters;

        private String startParameter;

        private RequestParams requestParams;

        public CommandParseResult(String commandName, String[] parameters) {
            this.commandName = commandName;
            this.parameters = parameters;
        }

        public CommandParseResult(String commandName, RequestParams requestParams) {
            this.commandName = commandName;
            this.requestParams = requestParams;
        }

        public CommandParseResult(String commandName, String startParameter) {
            this.commandName = commandName;
            this.startParameter = startParameter;
        }

        public String getCommandName() {
            return commandName;
        }

        public String[] getParameters() {
            return parameters;
        }

        public RequestParams getRequestParams() {
            return requestParams;
        }

        public String getStartParameter() {
            return startParameter;
        }

        public void setStartParameter(String startParameter) {
            this.startParameter = startParameter;
        }
    }
}
