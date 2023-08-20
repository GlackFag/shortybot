package com.glackfag.shortybot.bot;

import com.glackfag.shortybot.bot.action.Action;
import com.glackfag.shortybot.bot.action.ActionExecutor;
import com.glackfag.shortybot.bot.action.ActionRecognizer;
import com.glackfag.shortybot.util.UpdateUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
    @Getter
    private final String botUsername;
    @Getter
    private final String botToken;
    private final ActionRecognizer actionRecognizer;
    private final ActionExecutor actionExecutor;

    @Autowired
    public Bot(@Qualifier("botUsername") String botUsername,
               @Qualifier("botToken") String botToken,
               ActionRecognizer actionRecognizer, ActionExecutor actionExecutor,
               TelegramBotsApi api) {
        super(botToken);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.actionRecognizer = actionRecognizer;
        this.actionExecutor = actionExecutor;

        try {
            api.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Failed to register bot: " + e.getMessage());
            throw new Error(e);
        }
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Action action = actionRecognizer.recognize(update);
        actionExecutor.execute(action, update);
    }

}
