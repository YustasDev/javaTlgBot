package com.example.javatlgbot.service;

import com.example.javatlgbot.config.BotConfig;
import com.example.javatlgbot.model.CurrencyModel;
import com.example.javatlgbot.model.EUR;
import com.example.javatlgbot.model.Valute;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Scanner;

@Service
public class CurrencyService {

    public static CurrencyModel getCurrencyRate(String message, CurrencyModel model) throws IOException, ParseException {
        URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");  // https://www.cbr-xml-daily.ru/#json
        Scanner scanner = new Scanner((InputStream) url.getContent());
        String jsonString = "";
        while (scanner.hasNext()) {
            jsonString += scanner.nextLine();
        }
        Gson g = new Gson();
        CurrencyModel currencyModel = g.fromJson(jsonString, CurrencyModel.class);

        return currencyModel;
    }
}
