package com.glackfag.shortybot.bot.action;

import com.glackfag.shortybot.util.Commands;
import com.glackfag.shortybot.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

@Component
public class ActionRecognizer {
    @Value("${shorty.domain}")
    private String domain;

    public Action recognize(Update update) {
        Long userId = UpdateUtils.extractUserId(update);
        String userInput = UpdateUtils.extractUserInput(update);
        String callbackDataText = UpdateUtils.extractCallbackDataText(update);

        if (isStart(userInput))
            return Action.START;
        if (isSendMenu(userInput))
            return Action.SEND_MENU;
        if (isCancel(userInput))
            return Action.CANCEL;
        if (isSendShorteningGenerationForm(callbackDataText))
            return Action.SEND_SHORTENING_GENERATION_FORM;
        if (isReenterUrl(userId, userInput))
            return Action.ASK_REENTER_URL;
        if (isCreateNewAlias(userId, userInput))
            return Action.CREATE_NEW_ALIAS;
        if (isShowUsersAssociations(callbackDataText))
            return Action.SHOW_USERS_ASSOCIATIONS;
        if (isSendReportForm(callbackDataText))
            return Action.SEND_REPORT_FORM;
        if (isAskReenterReportUrl(userInput, userId))
            return Action.ASK_REENTER_REPORT_URL;
        if (isSubmitReport(userInput, userId))
            return Action.SUBMIT_REPORT;

        return Action.SEND_MENU;
    }

    private boolean isCancel(String userInput) {
        return userInput.equalsIgnoreCase(Commands.CANCEL);
    }

    private boolean isStart(String userInput) {
        return userInput.equals(Commands.START);
    }

    private boolean isSendMenu(String userInput) {
        return userInput.equals(Commands.MENU);
    }

    private boolean isSendShorteningGenerationForm(String callbackDataText) {
        return callbackDataText.equals(Commands.SHORTENING_FORM);
    }

    private boolean isCreateNewAlias(Long userId, String userInput) {
        return isUrl(userInput) && isUserCreatingAlias(userId);
    }

    private boolean isReenterUrl(Long userId, String userInput) {
        return !isUrl(userInput) && isUserCreatingAlias(userId);

    }

    private boolean isUserCreatingAlias(Long userId) {
        return Stream.of(Action.SEND_SHORTENING_GENERATION_FORM, Action.ASK_REENTER_URL).
                anyMatch(x -> x == ActionExecutor.getLastActions().get(userId));
    }

    private boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean isShowUsersAssociations(String callbackDataText) {
        return callbackDataText.equals(Commands.SHOW_USERS_ASSOCIATIONS);
    }

    private boolean isSendReportForm(String callbackDataText) {
        return callbackDataText.equals(Commands.REPORT_FORM);
    }

    private boolean isAskReenterReportUrl(String userInput, Long userId) {
        return !isShortyUrl(userInput) && isUserReporting(userId);
    }

    private boolean isSubmitReport(String userInput, Long userId) {
        return isShortyUrl(userInput) && isUserReporting(userId);
    }

    private boolean isUserReporting(Long userId) {
        return Stream.of(Action.SEND_REPORT_FORM, Action.ASK_REENTER_REPORT_URL).
                anyMatch(x -> x == ActionExecutor.getLastActions().get(userId));
    }

    private boolean isShortyUrl(String url) {
        if (!url.startsWith("http"))
            url = "https://" + url;
        try {
            return new URL(url).getHost().equals(domain);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
