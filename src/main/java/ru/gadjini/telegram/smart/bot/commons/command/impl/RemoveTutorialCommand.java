package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

@Component
public class RemoveTutorialCommand implements BotCommand, PaidSubscriptionOptional {

    private TutorialService tutorialService;

    private MessageService messageService;

    @Autowired
    public RemoveTutorialCommand(TutorialService tutorialService,
                                 @TgMessageLimitsControl MessageService messageService) {
        this.tutorialService = tutorialService;
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        tutorialService.delete(Integer.parseInt(params[0]));

        messageService.sendMessage(
                SendMessage.builder()
                        .chatId(String.valueOf(message.getFrom().getId()))
                        .text("Tutorial " + params[0] + " removed")
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.REMOVE_TUTORIAL;
    }
}
