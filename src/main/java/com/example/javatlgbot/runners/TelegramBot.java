package com.example.javatlgbot.runners;

import com.example.javatlgbot.config.BotConfig;
import com.example.javatlgbot.model.CurrencyModel;
import com.example.javatlgbot.model.User;
import com.example.javatlgbot.repository.UserRepository;
import com.example.javatlgbot.service.CryptoCurrencyService;
import com.example.javatlgbot.service.CurrencyService;
import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig botConfig;
    @Autowired
    private CurrencyModel currencyModel;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CryptoCurrencyService cryptoCurrencyService;

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
            String userName = update.getMessage().getChat().getFirstName();
            log.info("Request received: " + messageText);
            log.info("from: " + chatId);
          //  Object location = update.getMessage().getLocation();

            if(messageText.contains("/dispatch") && botConfig.getOwnerID() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (User user: users){
                    prepareAndSendMessage(user.getChatId(), textToSend);
                    return;
                }
            }

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, userName);
                    registerUser(update.getMessage());
                    break;
                case "/help":
                    helpAnswer(chatId, userName);
                    break;
                case "/get_crypto":
                    sendMessage(chatId, cryptoCurrencyService.loadCryptoCurrency());
                    break;
                case "/music":
                    startMusic(chatId, userName);
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
                        else {
                            String current_error = "Currency designation has been introduced: " + messageText;
                            log.error(current_error);
                            throw new IOException("The specified currency type was not found");
                        }
                    } catch (IOException e) {
                        sendMessage(chatId, "We have not found such a currency." + "\n" +
                                "Enter the currency whose official exchange rate" + "\n" +
                                "you want to know in relation to RUB." + "\n" +
                                "For example: USD, EUR or CNY");
                        File sourceimage = new File("Ein2.jpg");
                        InputFile img = new InputFile(sourceimage);
                        currency = "And you don't like rock, blues and classical music... Well ok.." + "\n\n" +
                                   "But Einstein looks at you reproachfully" + " :cry:";
                        String answer = EmojiParser.parseToUnicode(currency);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            log.error("The error occurred while executing 'Thread.sleep': " + e.getMessage());
                        }
                        sendMessage(chatId, answer);
                        sendImg(chatId, img);
                        break;
                    } catch (ParseException e) {
                        log.error(String.valueOf(e.getStackTrace()));
                        throw new RuntimeException("Unable to parse date");
                    }
                    sendMessage(chatId, currency);
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("rock")) {
                String text = "It's wonderful that you love ROCK!";
                executeEditMessageText(text, chatId, messageId);
            }
            else if (callbackData.equals("blues")) {
                String text = "It's wonderful that you love BLUES!";
                executeEditMessageText(text, chatId, messageId);
            }
            else if (callbackData.equals("classical")) {
                String text = "It's wonderful that you love classical music!";
                executeEditMessageText(text, chatId, messageId);
            }
            currency = "click on the button: " + "https://t.me/Mixvk_bot";
            sendMessage(chatId, currency);
        }

    }

    private void prepareAndSendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("The error occurred in the 'prepareAndSendMessage' method: " + e.getMessage());
        }
    }

    private void helpAnswer(Long chatId, String name){
        String answer = EmojiParser.parseToUnicode("Hey, " + name + "\n" +
                "I don't even know how to help you... " + "\n" +
                "just push all the buttons and you'll figure it out for yourself" + "\n" +
                " :bulb:");
        sendMessage(chatId, answer);
    }


    private void registerUser(Message message) {
        try {
        Optional <User> user = userRepository.findById(message.getChatId());
        if(!user.isPresent()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User new_user = new User();
            new_user.setChatId(chatId);
            new_user.setFirstName(chat.getFirstName());
            new_user.setLastName(chat.getLastName());
            new_user.setUserName(chat.getUserName());
            String timeStamp = new SimpleDateFormat("MM/dd/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
            new_user.setRegisterDate(timeStamp);
            userRepository.save(new_user);
        }
      }
        catch (Exception e){
            log.error("The error occurred while search user or saving the new User: " + e.getMessage());
        }

    }

    private void executeEditMessageText(String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("The error occurred in the 'executeEditMessageText' method: " + e.getMessage());
        }
    }



    private void startCommandReceived(Long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + "\n" +
                "Enter the currency whose official exchange rate" + "\n" +
                "you want to know in relation to RUB." + "\n" +
                "For example: USD, EUR or CNY " + "\n\n" +
                "But if you just want to listen to music, you can click '/music'" + " :grinning:");

        log.info("The name of the logged in user: " + name);
        sendMessage(chatId, answer);
    }

    private void startMusic(Long chatId, String name) {
        String answer = name + " I'm glad you love music!" + "\n" +
                "What kind of music do you want to listen to?" + "\n" +
                "For example: rock, blues or classical";

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var rock_Button = new InlineKeyboardButton();
        rock_Button.setText("ROCK");
        rock_Button.setCallbackData("rock");

        var blues_Button = new InlineKeyboardButton();
        blues_Button.setText("BLUES");
        blues_Button.setCallbackData("blues");

        var classical_Button = new InlineKeyboardButton();
        classical_Button.setText("CLASSICAL");
        classical_Button.setCallbackData("classical");

        rowInLine.add(rock_Button);
        rowInLine.add(blues_Button);
        rowInLine.add(classical_Button);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("An error occurred when sending a message from the 'startMusic' method: " + e.getMessage());
        }
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
