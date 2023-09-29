package com.example.javatlgbot.model;

import lombok.Data;
import java.util.Date;



@Data
public class ExchangeRate {
    public Date currentDate;
    public Date PreviousDate;
    public String PreviousURL;
    public Date Timestamp;
    public Valute valutes;


}
