package com.glackfag.shortybot.bot.action;

import com.glackfag.shortybot.util.Commands;
import com.glackfag.shortybot.util.UpdateUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.stream.Stream;

@Component
public class ActionRecognizer {
    public Action recognize(Update update) {
        Long userId = UpdateUtils.extractUserId(update);
        Message message = UpdateUtils.extractMessage(update);
        String callbackDataText = UpdateUtils.extractCallbackDataText(update);

        if (isStart(message))
            return Action.START;
        if (isSendMenu(message))
            return Action.SEND_MENU;
        if (isSendGenerationForm(callbackDataText))
            return Action.SEND_SHORTENING_GENERATION_FORM;
        if (isReenterUrl(userId, message))
            return Action.ASK_REENTER_URL;
        if (isCreateNewAlias(userId, message))
            return Action.CREATE_NEW_ALIAS;
        if (isShowUsersAssociations(callbackDataText))
            return Action.SHOW_USERS_ASSOCIATIONS;

        return Action.SEND_MENU;
    }

    private boolean isStart(Message message) {
        return message.getText().equals(Commands.START);
    }

    private boolean isSendMenu(Message message) {
        return message.getText().equals(Commands.MENU);
    }

    private boolean isSendGenerationForm(String callbackDataText) {
        return callbackDataText.equals(Commands.NEW_SHORTENING);
    }

    private boolean isCreateNewAlias(Long userId, Message message) {
        return isValidUrl(message.getText()) &&
                Stream.of(Action.SEND_SHORTENING_GENERATION_FORM, Action.ASK_REENTER_URL).
                        anyMatch(x -> x == ActionExecutor.getLastActions().get(userId));
    }

    private boolean isReenterUrl(Long userId, Message message) {
        return !isValidUrl(message.getText()) &&
                Stream.of(Action.SEND_SHORTENING_GENERATION_FORM, Action.ASK_REENTER_URL).
                        anyMatch(x -> x == ActionExecutor.getLastActions().get(userId));
    }

    private boolean isValidUrl(String url) {
        return url.matches("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    }

    private boolean isShowUsersAssociations(String callbackDataText) {
        return callbackDataText.equals(Commands.SHOW_USERS_ASSOCIATIONS);
    }
}
