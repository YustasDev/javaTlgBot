package com.example.javatlgbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BotConfig {

    final String botName;
    final String token;

    @Autowired
    public BotConfig(@Value("${bot.name}") String botName, @Value("${bot.token}") String token) {
        this.botName = botName;
        this.token = token;
    }
}
