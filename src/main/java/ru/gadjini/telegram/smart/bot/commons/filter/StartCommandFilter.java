package ru.gadjini.telegram.smart.bot.commons.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.gadjini.telegram.smart.bot.commons.annotation.KeyboardHolder;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.command.message.StartCommandMessageBuilder;
import ru.gadjini.telegram.smart.bot.commons.service.command.navigator.CommandNavigator;
import ru.gadjini.telegram.smart.bot.commons.service.keyboard.ReplyKeyboardService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.user.UserBotService;

import java.util.Locale;

@Component
public class StartCommandFilter extends BaseBotFilter {

    private CommandParser commandParser;

    private UserService userService;

    private MessageService messageService;

    private ReplyKeyboardService replyKeyboardService;

    private CommandNavigator commandNavigator;

    private StartCommandMessageBuilder startCommandMessageBuilder;

    private BotProperties botProperties;

    private UserBotService userBotService;

    @Autowired
    public StartCommandFilter(CommandParser commandParser, UserService userService,
                              @TgMessageLimitsControl MessageService messageService,
                              @KeyboardHolder ReplyKeyboardService replyKeyboardService,
                              CommandNavigator commandNavigator,
                              StartCommandMessageBuilder startCommandMessageBuilder,
                              BotProperties botProperties, UserBotService userBotService) {
        this.commandParser = commandParser;
        this.userService = userService;
        this.messageService = messageService;
        this.replyKeyboardService = replyKeyboardService;
        this.commandNavigator = commandNavigator;
        this.startCommandMessageBuilder = startCommandMessageBuilder;
        this.botProperties = botProperties;
        this.userBotService = userBotService;
    }

    @Override
    public void doFilter(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            CommandParser.CommandParseResult commandParseResult = commandParser.parseBotCommand(update.getMessage());
            if (commandParseResult.getCommandName().equals(CommandNames.START_COMMAND_NAME)) {
                boolean start = doStart(update.getMessage(), commandParseResult.getStartParameter());

                if (start) {
                    return;
                }
            }
        }

        super.doFilter(update);
    }

    private boolean doStart(Message message, String startParameter) {
        userService.createOrUpdate(message.getFrom(), startParameter);

        if (userBotService.create(message.getFrom().getId(), botProperties.getName())) {
            Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
            String text = startCommandMessageBuilder.getWelcomeMessage(locale);
            ReplyKeyboard mainMenu = replyKeyboardService.mainMenuKeyboard(message.getChatId(), locale);
            messageService.sendMessage(
                    SendMessage.builder().chatId(String.valueOf(message.getChatId())).text(text)
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(mainMenu).build()
            );

            commandNavigator.setCurrentCommand(message.getChatId(), CommandNames.START_COMMAND_NAME);

            return true;
        }

        return false;
    }
}
