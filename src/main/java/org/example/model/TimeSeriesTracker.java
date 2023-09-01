package org.example.model;

import org.example.persistence.IOhlcRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class TimeSeriesTracker {

    private Map<LocalDate, Map<String, BigDecimal>> priceMap;

    private IOhlcRepository ohlcRepository;

    public TimeSeriesTracker(IOhlcRepository ohlcRepository){
        this.ohlcRepository = ohlcRepository;
    }


    public void preparePriceData(Set<String> tickers, LocalDate startDate, LocalDate endDate){
        this.priceMap = new TreeMap<>();

        Set<String> verifiedTickers = new HashSet<>();

        for (String ticker: tickers){
            List<Ohlc> p = this.ohlcRepository.getOhlcFor(Timeframe.D1, ticker, startDate, endDate);

            if (p == null || p.isEmpty()){
                continue;
            }

            for (Ohlc ohlc: p){
                Map<String, BigDecimal> tickerMap = this.priceMap.getOrDefault(ohlc.getDate().toLocalDate(), new TreeMap<>());
                tickerMap.put(ticker, ohlc.getClose());
                this.priceMap.put(ohlc.getDate().toLocalDate(), tickerMap);
            }
        }
    }

    public Set<String> verifyTickers(Set<String> tickers){
        Set<String> verifiedTickers = new HashSet<>(tickers);
        for (String ticker: tickers){

            for (LocalDate date: this.priceMap.keySet()){
                if (!this.priceMap.get(date).containsKey(ticker)){
                    verifiedTickers.remove(ticker);
                }
            }
        }
        return verifiedTickers;
    }


    public Map<String, BigDecimal> getCurrentPrices(Set<String> tickers, LocalDate date){
        if (this.priceMap == null){
            this.preparePriceData(tickers, date, date);
        }
        return priceMap.get(date);
    }

    public BigDecimal getCurrentPrice(String ticker, LocalDate date){
        return priceMap.get(date).get(ticker);
    }
}
