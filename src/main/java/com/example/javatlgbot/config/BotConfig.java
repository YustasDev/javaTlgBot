package com.example.javatlgbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BotConfig {

    final private String botName;
    final private String token;
    final private Long ownerID;


    @Autowired
    public BotConfig(@Value("${bot.name}") String botName, @Value("${bot.token}") String token,
                     @Value("${bot.ownerID}") Long ownerID) {
        this.botName = botName;
        this.token = token;
        this.ownerID = ownerID;
    }
}
