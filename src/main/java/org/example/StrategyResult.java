package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StrategyResult {

    private List<Trade> trades;

    public StrategyResult(List<Trade> trades){
        this.trades = trades;
    }

    public StrategyResult(){
        this.trades = new ArrayList<>();
    }

    public List<Trade> getTrades(){
        return trades;
    }

    public void setTrades(List<Trade> trades){
        this.trades = trades;
    }

    public String toString(){
        return "StrategyResult(trades=" + this.getTrades() + ")";
    }

    public void addTrade(Trade trade){
        this.trades.add(trade);
    }

    public void addTrades(List<Trade> trades){
        this.trades.addAll(trades);
    }

    public void addTrades(StrategyResult strategyResult){
        this.trades.addAll(strategyResult.getTrades());
    }

    public BigDecimal getTotalProfit(){
        return this.trades.stream().map(Trade::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDividendProfit(){
        return this.trades.stream().map(Trade::getDividendProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalTradingProfit(){
        return this.trades.stream().map(Trade::getTradingProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalFee(){
        return this.trades.stream().map(Trade::getTradingFee).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTotalDays(){
        return this.trades.stream().map(Trade::getDays).reduce(0L, Long::sum);
    }

    public BigDecimal getAvgRoiYearlyAdjustedReturn(){
        BigDecimal totalDays = BigDecimal.valueOf(this.getTotalDays());
        BigDecimal totalProfit = this.getTotalProfit();
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalTrades = BigDecimal.valueOf(this.trades.size());
        BigDecimal daysPerTrade = totalDays.divide(totalTrades, 8, BigDecimal.ROUND_HALF_UP);

        BigDecimal returnOnTrade = profitPerTrade.divide(BigDecimal.valueOf(100));
        BigDecimal returnOnTradeYearlyAdjusted = returnOnTrade.multiply(BigDecimal.valueOf(365).divide(daysPerTrade, 8, BigDecimal.ROUND_HALF_UP));
        return returnOnTradeYearlyAdjusted;
    }

    public BigDecimal getAvgRoi(){
        BigDecimal totalDays = BigDecimal.valueOf(this.getTotalDays());
        BigDecimal totalProfit = this.getTotalProfit();
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalTrades = BigDecimal.valueOf(this.trades.size());
        BigDecimal daysPerTrade = totalDays.divide(totalTrades, 8, BigDecimal.ROUND_HALF_UP);

        BigDecimal returnOnTrade = profitPerTrade.divide(BigDecimal.valueOf(100));
        return returnOnTrade;
    }

    public String getSummary(){
        return "Total profit: " + this.getTotalProfit() + "\n" +
                "Total dividend profit: " + this.getTotalDividendProfit() + "\n" +
                "Total trading profit: " + this.getTotalTradingProfit() + "\n" +
                "Total trades: " + this.trades.size() + "\n" +
                "Total fee : " + this.getTotalFee() + "\n" +
                "Total days : " + this.getTotalDays() + "\n";
    }

}
