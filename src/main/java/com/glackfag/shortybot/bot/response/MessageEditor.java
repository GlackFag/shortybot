package com.glackfag.shortybot.bot.response;

import com.glackfag.shortybot.RestClient;
import com.glackfag.shortybot.bot.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MessageEditor {
    private final Bot bot;

    @Autowired
    public MessageEditor(RestClient restClient, @Lazy Bot bot) {
        this.bot = bot;
    }

    public void editFromSendMessage(int messageId, SendMessage sendMessage) throws TelegramApiException{
        edit(Long.parseLong(sendMessage.getChatId()), messageId,
                sendMessage.getText(), (InlineKeyboardMarkup) sendMessage.getReplyMarkup());
    }

    public void edit(long chatId, int messageId, String newText, InlineKeyboardMarkup markup) throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();

        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(newText);
        editMessage.setReplyMarkup(markup);

        bot.execute(editMessage);
    }
}
