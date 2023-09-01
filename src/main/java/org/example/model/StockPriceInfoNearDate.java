package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StockPriceInfoNearDate {
    private String name;
    private Map<Integer, Ohlc> ohlcAroundDividend;

    public StockPriceInfoNearDate(String name, Map<Integer, Ohlc> ohlcAroundDate) {
        this.ohlcAroundDividend = ohlcAroundDate;
        this.name = name;
    }

//    public Map<Integer, BigDecimal> getDailyReturnMap() {
//        Map<Integer, BigDecimal> daily = DailyReturnCalculator.getDailyReturnsFromMap(ohlcAroundDividend);
//        return daily;
//    }

    public Map<Integer, BigDecimal> getDailyReturnMap(){
        Map<Integer, BigDecimal> dailyReturns = new TreeMap<>();

        Integer smallestKey = ohlcAroundDividend.keySet().stream().min(Integer::compareTo).get();

        for (Map.Entry<Integer, Ohlc> entry: ohlcAroundDividend.entrySet()){
            if (entry.getKey() != smallestKey){
                dailyReturns.put(entry.getKey(), entry.getValue().getClose().divide(ohlcAroundDividend.get(entry.getKey() - 1).getClose(), 8, RoundingMode.FLOOR).subtract(BigDecimal.ONE));
            }
        }

        return dailyReturns;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "StockPriceInfoNearDate{" +
                "name='" + name + '\'' +
                ", ohlcAroundDividend=" + ohlcAroundDividend +
                '}' + getDailyReturnMap();
    }
}
