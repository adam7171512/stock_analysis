package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

public class StockNearDividendAggregator {

    Map<Integer, BigDecimal> dailyReturnsSum;
    BigDecimal dividendYieldSum;
    int aggregationCount;

    public StockNearDividendAggregator(){
        this.dailyReturnsSum = new TreeMap<>();
        this.dividendYieldSum = BigDecimal.ZERO;
    }

    public void add(StockPriceInfoNearDividend stockPriceInfoNearDividend){
        this.dividendYieldSum = this.dividendYieldSum.add(stockPriceInfoNearDividend.getDividendYield());

        for (Map.Entry<Integer, BigDecimal> dailyReturn : stockPriceInfoNearDividend.getDailyReturnMap().entrySet()) {
            if (this.dailyReturnsSum.containsKey(dailyReturn.getKey())){
                this.dailyReturnsSum.put(dailyReturn.getKey(), this.dailyReturnsSum.get(dailyReturn.getKey()).add(dailyReturn.getValue()));
            } else {
                this.dailyReturnsSum.put(dailyReturn.getKey(), dailyReturn.getValue());
            }
        }
        this.aggregationCount++;
    }

    public Map<Integer, BigDecimal> getAvgDailyReturns(){
        Map<Integer, BigDecimal> avgDailyReturns = new TreeMap<>();
        for (Map.Entry<Integer, BigDecimal> dailyReturn : this.dailyReturnsSum.entrySet()) {
            avgDailyReturns.put(dailyReturn.getKey(), dailyReturn.getValue().divide(new BigDecimal(this.aggregationCount), 8, RoundingMode.FLOOR));
        }
        return avgDailyReturns;
    }

    public Map<Integer, BigDecimal> getAvgDailyReturnsDividendAdjusted(){
        Map<Integer, BigDecimal> avgDailyReturns = new TreeMap<>();
        for (Map.Entry<Integer, BigDecimal> dailyReturn : this.dailyReturnsSum.entrySet()) {
            avgDailyReturns.put(dailyReturn.getKey(), dailyReturn.getValue().divide(new BigDecimal(this.aggregationCount), 8, RoundingMode.FLOOR));
        }
        avgDailyReturns.put(0, avgDailyReturns.get(0).add(getAvgDividendYieldTaxAdjusted()));
        return avgDailyReturns;
    }

    public BigDecimal getAvgDividendYield(){
        return this.dividendYieldSum.divide(new BigDecimal(this.aggregationCount), 8, RoundingMode.FLOOR);
    }

    public BigDecimal getAvgDividendYieldTaxAdjusted(){

        BigDecimal taxAdjusted = getAvgDividendYield().multiply(BigDecimal.ONE
                .subtract(FeeManager.getDividendTax())
        );
        return taxAdjusted;
    }

    public int getAggregationCount() {
        return aggregationCount;
    }




}
