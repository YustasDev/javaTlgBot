package com.example.javatlgbot.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;


@Data
public class ExchangeRateCrypto {
    Binancecoin binancecoin;
    Bitcoin bitcoin;
    Ethereum ethereum;
    Solana solana;
    @SerializedName("matic-network")
    MaticNetwork maticNetwork;

}
