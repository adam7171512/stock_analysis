package org.example.model;

import org.example.StrategyResult;

import java.util.Map;

public class SimplePlotData {

    Map<Integer, StrategyResult> strategyResultMap;
    int buyDate;
    String description;

    public SimplePlotData(Map<Integer, StrategyResult> strategyResultMap, int buyDate, String description) {
        this.strategyResultMap = strategyResultMap;
        this.buyDate = buyDate;
        this.description = description;
    }

    public Map<Integer, StrategyResult> getStrategyResultMap() {
        return strategyResultMap;
    }

    public void setStrategyResultMap(Map<Integer, StrategyResult> strategyResultMap) {
        this.strategyResultMap = strategyResultMap;
    }

    public int getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(int buyDate) {
        this.buyDate = buyDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
