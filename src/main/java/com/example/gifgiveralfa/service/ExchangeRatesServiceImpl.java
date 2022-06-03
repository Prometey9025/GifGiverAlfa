package com.example.gifgiveralfa.service;

import com.example.gifgiveralfa.client.OpenExchangeRatesClient;
import com.example.gifgiveralfa.model.ExchangeRates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

// сервис для работы с openexchangerates.org

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    private ExchangeRates prevRates;
    private ExchangeRates currentRates;

    private OpenExchangeRatesClient openExchangeRatesClient;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    @Value("${openexchangerates.app.id}")
    private String appId;
    @Value("${openexchangerates.base}")
    private String base;

    @Autowired
    public ExchangeRatesServiceImpl(
            OpenExchangeRatesClient openExchangeRatesClient,
            @Qualifier("date") SimpleDateFormat dateFormat,
            @Qualifier("time") SimpleDateFormat timeFormat
    ) {
        this.openExchangeRatesClient = openExchangeRatesClient;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }


    // возвращает список доступных для проверки валют.

    @Override
    public List<String> getCharCodes() {
        List<String> result = null;
        if (this.currentRates.getRates() != null) {
            result = new ArrayList<>(this.currentRates.getRates().keySet());
        }
        return result;
    }


     /* проверяет\обновляет курсы,
      возвращает результат сравнения коэфициентов,
      если курсов или коэфициентов нет возвращает -101*/

    @Override
    public int getKeyForTag(String charCode) {
        this.refreshRates();
        Double prevCoefficient = this.getCoefficient(this.prevRates, charCode);
        Double currentCoefficient = this.getCoefficient(this.currentRates, charCode);
        return prevCoefficient != null && currentCoefficient != null
                ? Double.compare(currentCoefficient, prevCoefficient)
                : -101;
    }

    //Проверка\обновление курсов

    @Override
    public void refreshRates() {
        long currentTime = System.currentTimeMillis();
        this.refreshCurrentRates(currentTime);
        this.refreshPrevRates(currentTime);
    }

     /* обновление текущих курсов,
      проверяется время с точностью до часа*/

    private void refreshCurrentRates(long time) {
        if (
                this.currentRates == null ||
                        !timeFormat.format(Long.valueOf(this.currentRates.getTimestamp()) * 1000)
                                .equals(timeFormat.format(time))
        ) {
            this.currentRates = openExchangeRatesClient.getLatestRates(this.appId);
        }
    }

    /* обновление вчерашних курсов,
       проверяется время с точностью до дня */
    private void refreshPrevRates(long time) {
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTimeInMillis(time);
        String currentDate = dateFormat.format(prevCalendar.getTime());
        prevCalendar.add(Calendar.DAY_OF_YEAR, -1);
        String newPrevDate = dateFormat.format(prevCalendar.getTime());
        if (
                this.prevRates == null
                        || (
                        !dateFormat.format(Long.valueOf(this.prevRates.getTimestamp()) * 1000)
                                .equals(newPrevDate)
                                && !dateFormat.format(Long.valueOf(this.prevRates.getTimestamp()) * 1000)
                                .equals(currentDate)
                )
        ) {
            this.prevRates = openExchangeRatesClient.getHistoricalRates(newPrevDate, appId);
        }
    }

    /* формула для подсчётка коэфициента
     (Default_Base / Our_Base) * Target */
    private Double getCoefficient(ExchangeRates rates, String charCode) {
        Double result = null;
        Double targetRate = null;
        Double appBaseRate = null;
        Double defaultBaseRate = null;
        Map<String, Double> map = null;
        if (rates != null && rates.getRates() != null) {
            map = rates.getRates();
            targetRate = map.get(charCode);
            appBaseRate = map.get(this.base);
            defaultBaseRate = map.get(rates.getBase());
        }
        if (targetRate != null && appBaseRate != null && defaultBaseRate != null) {
            result = new BigDecimal(
                    (defaultBaseRate / appBaseRate) * targetRate
            )
                    .setScale(4, RoundingMode.UP)
                    .doubleValue();
        }
        return result;
    }
}
