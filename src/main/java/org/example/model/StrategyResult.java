package org.example.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StrategyResult {

    private List<Trade> trades;

    public StrategyResult(List<Trade> trades){
        this.trades = trades;
    }

    public StrategyResult(){
        this.trades = new CopyOnWriteArrayList<>();
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
        BigDecimal totalProfit = this.getTotalProfit();
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, BigDecimal.ROUND_HALF_UP);

        BigDecimal returnOnTrade = profitPerTrade.divide(BigDecimal.valueOf(100));
        return returnOnTrade;
    }

    public BigDecimal getAvgProfitPerTrade(){
        BigDecimal totalProfit = this.getTotalProfit();
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, BigDecimal.ROUND_HALF_UP);
        return profitPerTrade;
    }

    public int getWinners(){
        return (int) this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) > 0).count();
    }

    public int getLosers(){
        return (int) this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) < 0).count();
    }

    public BigDecimal maxWinner(){
        return this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) > 0).map(Trade::getProfit).max(BigDecimal::compareTo).get();
    }

    public BigDecimal maxLoser(){
        return this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) < 0).map(Trade::getProfit).min(BigDecimal::compareTo).get();
    }

    public BigDecimal avgWinner(){
        return this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) > 0).map(Trade::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getWinners()), 8, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal avgLoser(){
        return this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) < 0).map(Trade::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(this.getLosers()), 8, BigDecimal.ROUND_HALF_UP);
    }

    public String getSummary(){
        return "Total profit: " + this.getTotalProfit() + "\n" +
                "Total dividend profit: " + this.getTotalDividendProfit() + "\n" +
                "Total trading profit: " + this.getTotalTradingProfit() + "\n" +
                "Total trades: " + this.trades.size() + "\n" +
                "Total fee : " + this.getTotalFee() + "\n" +
                "Total days : " + this.getTotalDays() + "\n" +
                "Avg ROI yearly adjusted return: " + this.getAvgRoiYearlyAdjustedReturn() + "\n" +
                "Avg ROI: " + this.getAvgRoi() + "\n" +
                "Winners: " + this.getWinners() + "\n" +
                "Losers: " + this.getLosers() + "\n" +
                "Max winner: " + this.maxWinner() + "\n" +
                "Max loser: " + this.maxLoser() + "\n" +
                "Avg winner: " + this.avgWinner() + "\n" +
                "Avg loser: " + this.avgLoser() + "\n";
    }

}
