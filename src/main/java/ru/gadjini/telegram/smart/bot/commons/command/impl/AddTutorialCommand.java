package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.NavigableBotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

@Component
public class AddTutorialCommand implements BotCommand, NavigableBotCommand {

    private UserService userService;

    private LocalisationService localisationService;

    private TutorialService tutorialService;

    private MessageService messageService;

    @Autowired
    public AddTutorialCommand(UserService userService, LocalisationService localisationService,
                              TutorialService tutorialService, @TgMessageLimitsControl MessageService messageService) {
        this.userService = userService;
        this.localisationService = localisationService;
        this.tutorialService = tutorialService;
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getFrom().getId()))
                        .text(localisationService.getMessage(
                                MessagesProperties.MESSAGE_ADD_TUTORIAL_WELCOME, userService.getLocaleOrDefault(message.getFrom().getId())
                        )).build()
        );
    }

    @Override
    public void processNonCommandUpdate(Message message, String text) {
        String[] args = text.split(";");

        Tutorial tutorial = new Tutorial();
        tutorial.setDescription(args[2]);
        tutorial.setFileId(args[1]);
        tutorial.setCommand(args[0]);

        tutorialService.create(tutorial);
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.ADD_TUTORIAL;
    }

    @Override
    public String getParentCommandName(long chatId) {
        return CommandNames.START_COMMAND_NAME;
    }

    @Override
    public String getHistoryName() {
        return getCommandIdentifier();
    }
}
