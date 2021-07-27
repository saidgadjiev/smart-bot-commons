package ru.gadjini.telegram.smart.bot.commons.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.command.api.BotCommand;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.UserService;
import ru.gadjini.telegram.smart.bot.commons.service.message.MediaMessageService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class PostCommand implements BotCommand {

    private MediaMessageService messageService;

    private LocalisationService localisationService;

    private UserService userService;

    private BotProperties botProperties;

    @Autowired
    public PostCommand(@Qualifier("mediaLimits") MediaMessageService messageService,
                       LocalisationService localisationService,
                       UserService userService, BotProperties botProperties) {
        this.messageService = messageService;
        this.localisationService = localisationService;
        this.userService = userService;
        this.botProperties = botProperties;
    }

    @Override
    public boolean accept(Message message) {
        return userService.isAdmin(message.getFrom().getId());
    }

    @Override
    public void processMessage(Message message, String[] params) {
        messageService.sendPhoto(
                SendPhoto.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .photo(new InputFile(new File("C:/logo.jpg")))
                        .caption(localisationService.getMessage("message.post",
                                new Object[]{botProperties.getName(), params[0]},
                                userService.getLocaleOrDefault(message.getFrom().getId())))
                        .replyMarkup(postMarkup(params[0]))
                        .parseMode(ParseMode.HTML)
                        .build()
        );
    }

    @Override
    public String getCommandIdentifier() {
        return CommandNames.POST;
    }

    private InlineKeyboardMarkup postMarkup(String reffreal) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(new ArrayList<>());
        InlineKeyboardButton joinButton = new InlineKeyboardButton("Start");
        joinButton.setUrl("https://t.me/" + botProperties.getName() + "?start=" + reffreal);

        inlineKeyboardMarkup.getKeyboard().add(List.of(joinButton));

        return inlineKeyboardMarkup;
    }
}
