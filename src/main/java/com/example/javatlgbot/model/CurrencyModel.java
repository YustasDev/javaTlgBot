package com.example.javatlgbot.model;

import lombok.Data;
import java.util.Date;

@Data
public class CurrencyModel {
    public Date Date;
    public Date PreviousDate;
    public String PreviousURL;
    public Date Timestamp;
    public Valute Valute;
}
