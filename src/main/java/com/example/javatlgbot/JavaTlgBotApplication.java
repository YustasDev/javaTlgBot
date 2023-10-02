package com.example.javatlgbot;

import com.example.javatlgbot.config.BotConfig;
import com.example.javatlgbot.model.CurrencyModel;
import com.example.javatlgbot.model.Valute;
import com.example.javatlgbot.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.ParseException;

@SpringBootApplication
public class JavaTlgBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaTlgBotApplication.class, args);
    }
}
