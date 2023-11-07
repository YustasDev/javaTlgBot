package com.example.javatlgbot.service;


import com.example.javatlgbot.model.CurrencyModel;
import com.example.javatlgbot.model.ExchangeRateCrypto;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CryptoCurrencyService {

    private final String cryptocurrencies;

    public CryptoCurrencyService(@Value("${cryptocurrencies}")String cryptocurrencies) {
        this.cryptocurrencies = cryptocurrencies;
    }

    public String loadCryptoCurrency() {
        String answer = "Information about the exchange rate of cryptocurrencies has not been received";
        String uri = cryptocurrencies;
        try {
            answer = makeAPICall(uri);
        }
        catch (Exception e) {
            System.err.println("Error: cannont access content:  " + e.toString());
            log.error("An error occurred when receiving the cryptocurrency exchange rate: " + e.getMessage());
        }
        return answer;
    }

    private String makeAPICall(String uri) throws URISyntaxException, IOException {
        String response_content = "";
        String now = new java.util.Date().toString();

        URIBuilder query = new URIBuilder(uri);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        CloseableHttpResponse response = client.execute(request);

        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("The error occurred in the CryptoCurrencyService: " + e.getMessage());
        }
        finally {
            response.close();
        }
        Gson g = new Gson();
        ExchangeRateCrypto exchangeRateCrypto = g.fromJson(response_content, ExchangeRateCrypto.class);
        String cryptoAnswer = "The exchange rate of the main cryptocurrencies as of '" +"\n" + now + "\n"
                              + "' according to the integrator 'Coingecko.com' is: " + "\n"
                              + "one Bitcoin = " + exchangeRateCrypto.getBitcoin().getUsd() + "$ \n"
                              + "one Ethereum = " + exchangeRateCrypto.getEthereum().getUsd() + "$ \n"
                              + "one BNB(Binancecoin) = " + exchangeRateCrypto.getBinancecoin().getUsd() + "$ \n"
                              + "one Solana = " + exchangeRateCrypto.getSolana().getUsd() + "$ \n"
                              + "one Polygon(matic-network) = " + exchangeRateCrypto.getMaticNetwork().getUsd() + "$ \n";
        return cryptoAnswer;
    }



}
