package com.glackfag.shortybot.bot.action;

import com.glackfag.shortybot.RestClient;
import com.glackfag.shortybot.bot.Bot;
import com.glackfag.shortybot.bot.response.MessageEditor;
import com.glackfag.shortybot.bot.response.ResponseGenerator;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.util.AutoDeletingConcurrentHashMap;
import com.glackfag.shortybot.util.UpdateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.NoSuchElementException;

@Component
@Slf4j
public class ActionExecutor {
    private final Bot bot;
    private final ResponseGenerator responseGenerator;
    private final MessageEditor messageEditor;
    private final RestClient restClient;

    private static final AutoDeletingConcurrentHashMap<Long, Action> lastActions = new AutoDeletingConcurrentHashMap<>(300_000L);

    @Autowired
    public ActionExecutor(@Lazy Bot bot, ResponseGenerator responseGenerator, RestClient restClient, MessageEditor messageEditor) {
        this.bot = bot;
        this.responseGenerator = responseGenerator;
        this.restClient = restClient;
        this.messageEditor = messageEditor;
    }


    public void execute(Action action, Update update) throws TelegramApiException {
        Long userId = UpdateUtils.extractUserId(update);
        Long chatId = UpdateUtils.extractChatId(update);

        try {
            switch (action) {
                case START, SEND_MENU, ASK_REENTER_URL -> bot.execute(responseGenerator.generate(update, action));
                case CREATE_NEW_ALIAS -> createNewAlias(userId, chatId, UpdateUtils.extractUserInput(update));

                default -> {
                    SendMessage message = responseGenerator.generate(update, action);
                    messageEditor.editFromSendMessage(UpdateUtils.extractMessage(update).getMessageId(), message);
                }
            }
        }catch (Exception e){
            log.debug(e.getMessage());
            throw e;
        }

        lastActions.put(userId, action);

        log.info(String.format("User:%d action:%s", userId, action));
    }

    static AutoDeletingConcurrentHashMap<Long, Action> getLastActions() {
        return lastActions;
    }

    private void createNewAlias(Long userId, Long chatId, String destination) throws TelegramApiException {
        Association association = new Association();
        association.setDestination(destination);
        association.setCreatorId(userId);

        association = restClient.createAuto(association).orElseThrow();

        SendMessage sendMessage = new SendMessage(chatId.toString(),
                String.format("Your shortening: https://shorty.su/%s", association.getAlias()));

        bot.execute(sendMessage);
    }
}
