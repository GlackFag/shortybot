package com.glackfag.shortybot.util;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public abstract class UpdateUtils {
    public static Long extractChatId(Update update) {
        Message m = update.getMessage();
        if (m != null)
            return m.getChatId();
        else
            return update.getCallbackQuery().getMessage().getChatId();
    }

    public static Long extractUserId(Update update) {
        Message m = update.getMessage();
        if (m != null)
            return m.getFrom().getId();
        else
            return update.getCallbackQuery().getFrom().getId();
    }

    public static String extractUserInput(Update update) {
        try {
            return update.getMessage().getText();
        } catch (NullPointerException e) {
            return "";
        }
    }

    public static String extractCallbackDataText(Update update) {
        try {
            return update.getCallbackQuery().getData();
        } catch (NullPointerException e) {
            return "";
        }
    }

    public static InlineKeyboardButton getUsedInlineButton(Update update) {
        if (!update.hasCallbackQuery())
            return null;

        String data = extractCallbackDataText(update);
        List<List<InlineKeyboardButton>> rows = update.getCallbackQuery().getMessage().getReplyMarkup().getKeyboard();

        for (List<InlineKeyboardButton> row : rows)
            for (InlineKeyboardButton e : row)
                if (e.getCallbackData().equals(data))
                    return e;

        return null;
    }

    public static Message extractMessage(Update update){
        if(update.hasCallbackQuery())
            return update.getCallbackQuery().getMessage();
        else
            return update.getMessage();
    }
}
