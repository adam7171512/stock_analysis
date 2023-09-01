package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyReturnCalculator {


    public static Map<Integer, BigDecimal> getDailyReturnsFromMap(Map<Integer, Ohlc> ohlcMap){
        Map<Integer, BigDecimal> dailyReturns = new HashMap<>();

        Integer smallestKey = ohlcMap.keySet().stream().min(Integer::compareTo).get();

        for (Map.Entry<Integer, Ohlc> entry: ohlcMap.entrySet()){
            if (entry.getKey() == smallestKey){
                continue;
            } else {
                dailyReturns.put(entry.getKey(), entry.getValue().getClose().divide(ohlcMap.get(entry.getKey() - 1).getClose(), 8, RoundingMode.FLOOR).subtract(BigDecimal.ONE));
            }
        }

        return dailyReturns;
    }
}
