package org.example.model;

import com.sun.source.tree.Tree;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class StockPriceInfoNearDividend extends StockPriceInfoNearDate{
    private BigDecimal dividendYield;


    public StockPriceInfoNearDividend(Map<Integer, Ohlc> ohlcMap, model.Dividend dividend){
        super(dividend.getName(), ohlcMap);
        this.dividendYield = dividend.getYield();
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public Map<Integer, BigDecimal> getDailyReturnMapAdjustedForDividend(){
        Map<Integer, BigDecimal> mapAdjustedForDividend = new TreeMap<>(this.getDailyReturnMap());
        if (mapAdjustedForDividend.get(0) != null)
        {
            mapAdjustedForDividend.put(0, this.dividendYield.multiply(BigDecimal.ONE.subtract(FeeManager.getDividendTax())));
        }
        return mapAdjustedForDividend;
    }
}
