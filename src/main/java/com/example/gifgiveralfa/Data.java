package com.example.gifgiveralfa;

import com.example.gifgiveralfa.service.ExchangeRatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Data {

    private ExchangeRatesService exchangeRatesService;

    @Autowired
    public Data(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @PostConstruct
    public void firstDataInit() {
        exchangeRatesService.refreshRates();
    }


}
