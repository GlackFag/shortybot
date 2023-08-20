package com.glackfag.shortybot.bot.response;

import com.glackfag.shortybot.RestClient;
import com.glackfag.shortybot.bot.action.Action;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.util.UpdateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

@Component
public class ResponseGenerator {
    private final ApplicationContext context;
    private final RestClient restClient;

    @Autowired
    public ResponseGenerator(ApplicationContext context, RestClient restClient) {
        this.context = context;
        this.restClient = restClient;
    }

    public SendMessage generate(Update update, Action action) {
        long userId = UpdateUtils.extractUserId(update);

        SendMessage message = new SendMessage();
        message.setChatId(UpdateUtils.extractChatId(update));

        switch (action) {
            case START, SEND_MENU -> {
                message.setText("Menu");
                message.setReplyMarkup((ReplyKeyboard) context.getBean("menuMarkup"));
            }
            case SEND_SHORTENING_GENERATION_FORM ->
                    message.setText("Enter url you want to get shorted. For example: https://shorty.su");
            case ASK_REENTER_URL ->
                    message.setText("No-no-no url you entered isn't valid.\nDon't forget about 'https://'");
            case SHOW_USERS_ASSOCIATIONS -> {
                List<Association> associationList = restClient.readAllAssociationsByCreatorId(userId);
                StringBuilder text = new StringBuilder();

                if (associationList.isEmpty())
                    text.append("You don't have any shortenings yet.");
                else {
                    associationList.stream().map(x -> String.format("Alias: shorty.su/%s%nDestination: %s%n%n", x.getAlias(), x.getDestination())).forEach(text::append);
                    int length = text.length();
                    text.delete(length - 2, length);
                }
                text.trimToSize();
                message.setText(text.toString());
            }
        }
        message.setParseMode("Markdown");

        return message;
    }
}
