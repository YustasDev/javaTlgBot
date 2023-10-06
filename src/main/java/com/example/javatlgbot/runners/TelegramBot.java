package com.example.javatlgbot.runners;

import com.example.javatlgbot.config.BotConfig;
import com.example.javatlgbot.model.CurrencyModel;
import com.example.javatlgbot.model.Valute;
import com.example.javatlgbot.service.CurrencyService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
@AllArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig botConfig;
    @Autowired
    private CurrencyModel currencyModel;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        String currency = "";

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/music":
                    startMusic(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default:
                    try {
                        CurrencyModel currencyRate = CurrencyService.getCurrencyRate(messageText, currencyModel);
                        String today = String.valueOf(currencyRate.getTimestamp());
                        if(messageText.equals("USD")){
                            currency = "Official rate of USD to RUB on the date: " + "\n"
                                       + today + "\n"
                                       + " is " + "'" + currencyRate.getValute().getUSD().getValue() + " rubles'" + "\n"
                                       + " for " + currencyRate.getValute().getUSD().getNominal() + " "
                                       + currencyRate.getValute().getUSD().getName() + "\n";
                        }
                        else if(messageText.equals("EUR")){
                            currency = "Official rate of EUR to RUB on the date: " + "\n"
                                    + today + "\n"
                                    + " is " + "'" + currencyRate.getValute().getEUR().getValue() + " rubles'" + "\n"
                                    + " for " + currencyRate.getValute().getEUR().getNominal() + " "
                                    + currencyRate.getValute().getEUR().getName() + "\n";
                        }
                        else if(messageText.equals("CNY")){
                            currency = "Official rate of Chinese Yuan to RUB on the date: " + "\n"
                                    + today + "\n"
                                    + " is " + "'" + currencyRate.getValute().getCNY().getValue() + " rubles'" + "\n"
                                    + " for " + currencyRate.getValute().getCNY().getNominal() + " "
                                    + currencyRate.getValute().getCNY().getName() + "\n";
                        }
//                        else if(messageText.equals("PIC")){
//                              File sourceimage = new File("/home/progforce/Pictures/Ein2.jpg");
//                              InputFile img = new InputFile(sourceimage);
//                              sendMessage(chatId, "Einstein looks at you reproachfully");
//                              sendImg(chatId, img);
//                              return;
//                        }
                        else if(messageText.equals("rock") || messageText.equals("blues") || messageText.equals("classical")) {
                            currency = "click on the button: " + "https://t.me/KDMusic_Bot";
                        }
//                        else {
//                            currency = "That is, do you not like rock, blues and classical music? Well ok.." + "\n"
//                                    + "click on the button: " + "https://t.me/lightmusic2bot";
//                        }
                        else {
                            //String timeStamp = new SimpleDateFormat("MM/dd/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
                            String current_error = "Currency designation has been introduced: " + messageText;
                            log.error(current_error);
                            throw new IOException("The specified currency type was not found");
                        }
                    } catch (IOException e) {
                        sendMessage(chatId, "We have not found such a currency." + "\n" +
                                "Enter the currency whose official exchange rate" + "\n" +
                                "you want to know in relation to RUB." + "\n" +
                                "For example: USD, EUR or CNY");
                        File sourceimage = new File("/home/progforce/Pictures/Ein2.jpg");
                        InputFile img = new InputFile(sourceimage);
                        currency = "And you don't like rock, blues and classical music..." + "\n" +
                                   " Einstein looks at you reproachfully";
                        sendImg(chatId, img);
                    } catch (ParseException e) {
                        log.error(String.valueOf(e.getStackTrace()));
                        throw new RuntimeException("Unable to parse date");
                    }
                    sendMessage(chatId, currency);
            }
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!" + "\n" +
                "Enter the currency whose official exchange rate" + "\n" +
                "you want to know in relation to RUB." + "\n" +
                "For example: USD, EUR or CNY " + "\n" +
                "but if you just want to listen to music, you can click '/music'";
        log.info("The name of the logged in user: " + name);
        sendMessage(chatId, answer);
    }

    private void startMusic(Long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!" + "\n" +
                "I'm glad you love music!" + "\n" +
                "What kind of music do you want to listen to?" + "\n" +
                "For example: rock, blues or classical";
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(String.valueOf(e.getStackTrace()));
        }
    }

    private void sendImg(Long chatId, InputFile img) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(img);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(String.valueOf(e.getStackTrace()));
        }
    }


}
