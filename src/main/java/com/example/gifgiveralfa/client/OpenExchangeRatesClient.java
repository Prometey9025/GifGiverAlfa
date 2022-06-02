package com.example.gifgiveralfa.client;

import com.example.gifgiveralfa.model.ExchangeRates;

public interface OpenExchangeRatesClient {

    ExchangeRates getLatestRates(String appId);

    ExchangeRates getHistoricalRates(String date, String appId);

}
