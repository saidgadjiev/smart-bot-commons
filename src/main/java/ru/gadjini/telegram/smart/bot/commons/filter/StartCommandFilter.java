package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.CreateOrUpdateResult;
import ru.gadjini.telegram.smart.bot.commons.model.TgMessage;
import ru.gadjini.telegram.smart.bot.commons.service.CommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;

@Component
public class StartCommandFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private UserService userService;

    private MessageService messageService;

    private LocalisationService localisationService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private CommandMessageBuilder commandMessageBuilder;

    @Autowired
    public StartCommandFilter(CommandParser commandParser, UserService userService,
                              @Qualifier("messageLimits") MessageService messageService, LocalisationService localisationService,
                              @Qualifier("curr") ReplyKeyboardService replyKeyboardService, CommandNavigator commandNavigator, CommandMessageBuilder commandMessageBuilder) {
        this.commandParser = commandParser;
        this.userService = userService;
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.replyKeyboardService = replyKeyboardService;
        this.commandNavigator = commandNavigator;
        this.commandMessageBuilder = commandMessageBuilder;
    }

    @Override
    public void doFilter(Update update) {
        if (isStartCommand(update)) {
            CreateOrUpdateResult createOrUpdateResult = doStart(TgMessage.from(update));

            if (createOrUpdateResult.isCreated()) {
                return;
            }
        }

        super.doFilter(update);
    }

    private boolean isStartCommand(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            String commandName = commandParser.parseBotCommandName(update.getMessage());

            return commandName.equals(CommandNames.START_COMMAND_NAME);
        }

        return false;
    }

    private CreateOrUpdateResult doStart(TgMessage message) {
        CreateOrUpdateResult createOrUpdateResult = userService.createOrUpdate(message.getUser());

        if (createOrUpdateResult.isCreated()) {
            String text = localisationService.getMessage(MessagesProperties.MESSAGE_WELCOME,
                    new Object[]{commandMessageBuilder.getCommandsInfo(createOrUpdateResult.getUser().getLocale())},
                    createOrUpdateResult.getUser().getLocale());
            ReplyKeyboard mainMenu = replyKeyboardService.getMainMenu(message.getChatId(), createOrUpdateResult.getUser().getLocale());
            messageService.sendMessage(
                    SendMessage.builder().chatId(String.valueOf(message.getChatId())).text(text)
                            .replyMarkup(mainMenu).build()
            );

            commandNavigator.setCurrentCommand(message.getChatId(), CommandNames.START_COMMAND_NAME);
        }

        return createOrUpdateResult;
    }
}
