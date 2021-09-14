package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.annotation.TgMessageLimitsControl;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.domain.Tutorial;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MessageService;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TutorialsCommand implements BotCommand {

    private LocalisationService localisationService;

    private TutorialService tutorialService;

    private UserService userService;

    private MessageService messageService;

    @Autowired
    public TutorialsCommand(LocalisationService localisationService, TutorialService tutorialService,
                            UserService userService, @TgMessageLimitsControl MessageService messageService) {
        this.localisationService = localisationService;
        this.tutorialService = tutorialService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        List<Tutorial> tutorials = tutorialService.getTutorials();
        Locale locale = userService.getLocaleOrDefault(message.getFrom().getId());
        if (tutorials.isEmpty()) {
            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(message.getFrom().getId()))
                            .text(localisationService.getMessage(MessagesProperties.MESSAGE_NO_TUTORIALS, locale))
                            .build()
            );
        } else {
            String text = localisationService.getMessage(MessagesProperties.MESSAGE_TUTORIALS,
                    new Object[]{buildTutorials(tutorials)}, locale);

            messageService.sendMessage(
                    SendMessage.builder()
                            .chatId(String.valueOf(message.getFrom().getId()))
                            .text(text)
                            .build()
            );
        }
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.TUTORIALS_COMMAND;
    }

    private String buildTutorials(List<Tutorial> tutorials) {
        Map<String, List<Tutorial>> tutorialsByCommand = tutorials.stream().collect(Collectors.groupingBy(Tutorial::getCommand));
        StringBuilder tutorialsBuilder = new StringBuilder();

        tutorialsByCommand.forEach((s, t) -> {
            tutorialsBuilder.append("For /").append(s).append(" command:\n");
            for (int i = 0; i < t.size(); i++) {
                Tutorial tutorial = t.get(i);
                tutorialsBuilder.append(i).append(") /tutorial_").append(tutorial.getId()).append(" - <i>")
                        .append(tutorial.getDescription()).append("</i>").append("\n");
            }
            tutorialsBuilder.append("\n");
        });

        return tutorialsBuilder.toString();
    }
}
