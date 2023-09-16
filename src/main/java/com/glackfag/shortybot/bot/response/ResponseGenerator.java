package com.glackfag.shortybot.bot.response;

import com.glackfag.shortybot.bot.action.Action;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.services.AssociationService;
import com.glackfag.shortybot.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
public class ResponseGenerator {
    private final ApplicationContext context;
    private final AssociationService associationService;

    @Autowired
    public ResponseGenerator(ApplicationContext context, AssociationService associationService) {
        this.context = context;
        this.associationService = associationService;
    }

    public SendMessage generate(Update update, Action action) {
        long userId = UpdateUtils.extractUserId(update);

        SendMessage message = new SendMessage();
        message.setChatId(UpdateUtils.extractChatId(update));

        switch (action) {
            case START, SEND_MENU, CANCEL -> {
                message.setText("Menu");
                message.setReplyMarkup((InlineKeyboardMarkup) context.getBean("menuMarkup"));
            }
            case SEND_SHORTENING_GENERATION_FORM ->
                    message.setText("Enter url you want to get shorted. For example: https://shorty.su\nOr type 'Cancel'");
            case ASK_REENTER_URL ->
                    message.setText("Url you entered is invalid");
            case SHOW_USERS_ASSOCIATIONS -> {
                List<Association> associationList = associationService.readAllAssociationsByCreatorId(userId);
                StringBuilder text = new StringBuilder();

                if (associationList.isEmpty())
                    text.append("You don't have any shortenings yet.");
                else {
                    associationList.stream().map(x -> String.format("Alias: shorty.su/%s%nDestination: %s%n%n",
                            x.getAlias(), x.getDestination())).forEach(text::append);
                    int length = text.length();
                    text.delete(length - 2, length);
                }
                text.trimToSize();
                message.setText(text.toString());
            }
            case SEND_REPORT_FORM ->
                message.setText("If you know a shortening leading to a malicious site, enter the url of it to send a report.\n Or type 'Cancel'");
            case ASK_REENTER_REPORT_URL ->
                message.setText("URL you entered not found");
        }


        message.setParseMode("Markdown");

        return message;
    }
}
