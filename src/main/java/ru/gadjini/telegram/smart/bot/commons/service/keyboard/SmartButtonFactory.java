package ru.gadjini.telegram.smart.bot.commons.service.keyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gadjini.telegram.smart.bot.commons.common.CommandNames;
import ru.gadjini.telegram.smart.bot.commons.common.MessagesProperties;
import ru.gadjini.telegram.smart.bot.commons.model.UploadType;
import ru.gadjini.telegram.smart.bot.commons.request.Arg;
import ru.gadjini.telegram.smart.bot.commons.service.LocalisationService;
import ru.gadjini.telegram.smart.bot.commons.service.command.CommandParser;
import ru.gadjini.telegram.smart.bot.commons.service.request.RequestParams;
import ru.gadjini.telegram.smart.bot.commons.service.telegram.TgUrlBuilder;

import java.util.Locale;

@Service
public class SmartButtonFactory {

    private LocalisationService localisationService;

    @Autowired
    public SmartButtonFactory(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    public InlineKeyboardButton updateQueryStatus(int queryItemId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.UPDATE_QUERY_STATUS_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.UPDATE_QUERY_STATUS + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.QUEUE_ITEM_ID.getKey(), queryItemId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton cancelQueryItem(int queryItemId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.CANCEL_QUERY_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.CANCEL_QUERY_COMMAND_NAME + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.QUEUE_ITEM_ID.getKey(), queryItemId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton doSmartUpload(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DO_SMART_UPLOAD_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.GET_SMART_FILE + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton streamingVideo(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.VIDEO_SUPPORTS_STREAMING_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.UPLOAD_TYPE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.STREAMING_VIDEO.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton document(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.DOCUMENT_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.UPLOAD_TYPE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.DOCUMENT.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }

    public InlineKeyboardButton goToPaymentBot(String paymentBotName, Locale locale) {
        InlineKeyboardButton button = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.MESSAGES_BUY_SUBSCRIPTION_COMMAND_DESCRIPTION, locale));
        button.setUrl(new TgUrlBuilder().tMe(paymentBotName));

        return button;
    }

    public InlineKeyboardButton video(int uploadId, Locale locale) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(localisationService.getMessage(MessagesProperties.VIDEO_COMMAND_DESCRIPTION, locale));
        inlineKeyboardButton.setCallbackData(CommandNames.UPLOAD_TYPE_COMMAND + CommandParser.COMMAND_NAME_SEPARATOR +
                new RequestParams()
                        .add(Arg.UPLOAD_TYPE.getKey(), UploadType.VIDEO.name())
                        .add(Arg.QUEUE_ITEM_ID.getKey(), uploadId)
                        .serialize(CommandParser.COMMAND_ARG_SEPARATOR));

        return inlineKeyboardButton;
    }
}
