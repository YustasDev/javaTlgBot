package com.example.javatlgbot.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class CurrencyModel {
    public Date Date;
    public Date PreviousDate;
    public String PreviousURL;
    public Date Timestamp;
    public Valute Valute;
}
