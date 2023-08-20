package com.glackfag.shortybot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.glackfag.shortybot.util.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@SpringBootApplication
public class ShortyBotApplication {
    private final Environment environment;

    @Autowired
    public ShortyBotApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(ShortyBotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());

        return objectMapper;
    }

    @Bean
    public String botToken() {
        return environment.getRequiredProperty("bot.token");
    }

    @Bean
    public String botUsername() {
        return environment.getRequiredProperty("bot.username");
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public InlineKeyboardMarkup menuMarkup() {
        InlineKeyboardButton newShortening = new InlineKeyboardButton("New shortening");
        newShortening.setCallbackData(Commands.NEW_SHORTENING);

        InlineKeyboardButton showUsersShortenings = new InlineKeyboardButton("My shortenings");
        showUsersShortenings.setCallbackData(Commands.SHOW_USERS_ASSOCIATIONS);

        return new InlineKeyboardMarkup(List.of(List.of(newShortening, showUsersShortenings)));
    }
}
