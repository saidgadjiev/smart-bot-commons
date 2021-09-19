package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.command.api.PaidSubscriptionOptional;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;
import ru.gadjini.telegram.smart.bot.commons.service.tutorial.TutorialService;

@Component
public class GetTutorialCommand implements BotCommand, PaidSubscriptionOptional {

    private MediaMessageService mediaMessageService;

    private TutorialService tutorialService;

    @Autowired
    public GetTutorialCommand(@Qualifier("mediaLimits") MediaMessageService mediaMessageService,
                              TutorialService tutorialService) {
        this.mediaMessageService = mediaMessageService;
        this.tutorialService = tutorialService;
    }

    @Override
    public void processMessage(Message message, String[] params) {
        String fileId = tutorialService.getFileId(Integer.parseInt(params[0]));
        if (StringUtils.isNotBlank(fileId)) {
            mediaMessageService.sendVideo(
                    SendVideo.builder()
                            .chatId(String.valueOf(message.getFrom().getId()))
                            .video(new InputFile(fileId))
                            .build()
            );
        }
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.GET_TUTORIAL;
    }
}
