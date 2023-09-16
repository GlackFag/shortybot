package com.glackfag.shortybot.bot.action;

import com.glackfag.shortybot.bot.Bot;
import com.glackfag.shortybot.bot.response.MessageEditor;
import com.glackfag.shortybot.bot.response.ResponseGenerator;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.models.Report;
import com.glackfag.shortybot.services.AssociationService;
import com.glackfag.shortybot.util.AutoDeletingConcurrentHashMap;
import com.glackfag.shortybot.util.UpdateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class ActionExecutor {
    private final Bot bot;
    private final ResponseGenerator responseGenerator;
    private final MessageEditor messageEditor;
    private final AssociationService associationService;

    private static final AutoDeletingConcurrentHashMap<Long, Action> lastActions = new AutoDeletingConcurrentHashMap<>(300_000L);

    @Autowired
    public ActionExecutor(@Lazy Bot bot, ResponseGenerator responseGenerator, AssociationService associationService, MessageEditor messageEditor) {
        this.bot = bot;
        this.responseGenerator = responseGenerator;
        this.associationService = associationService;
        this.messageEditor = messageEditor;
    }

    public void execute(Action action, Update update) throws TelegramApiException {
        Long userId = UpdateUtils.extractUserId(update);
        Long chatId = UpdateUtils.extractChatId(update);

        log.info(String.format("User:%d action:%s", userId, action));

        try {
            switch (action) {
                case START, SEND_MENU, CANCEL, ASK_REENTER_URL, SEND_REPORT_FORM, ASK_REENTER_REPORT_URL ->
                        bot.execute(responseGenerator.generate(update, action));
                case CREATE_NEW_ALIAS -> createNewAlias(userId, chatId, UpdateUtils.extractUserInput(update));
                case SUBMIT_REPORT ->
                        submitReport(UpdateUtils.extractUserInput(update).replaceFirst("^([\\w.:]+/+)+", ""), userId, chatId); // https://remove.all/before/this -> this
                default -> {
                    SendMessage message = responseGenerator.generate(update, action);
                    messageEditor.editFromSendMessage(UpdateUtils.extractMessage(update).getMessageId(), message);
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }

        lastActions.put(userId, action);
    }

    static AutoDeletingConcurrentHashMap<Long, Action> getLastActions() {
        return lastActions;
    }

    private void createNewAlias(Long userId, Long chatId, String destination) throws TelegramApiException {
        Association association = new Association();
        association.setDestination(destination);
        association.setCreatorId(userId);

        association = associationService.createAuto(association);

        SendMessage sendMessage = new SendMessage(chatId.toString(),
                String.format("Your shortening: shorty.su/%s", association.getAlias()));

        bot.execute(sendMessage);
    }

    private void submitReport(String alias, long userId, Long chatId) throws TelegramApiException {
        boolean isReportSuccessful = associationService.report(new Report(alias, userId));
        SendMessage message = new SendMessage(chatId.toString(),
                isReportSuccessful ? "Reported successfully." : "You already reported this shortening or it doesn't exist.");

        bot.execute(message);
    }
}
