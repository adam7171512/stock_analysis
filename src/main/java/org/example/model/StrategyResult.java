package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, RoundingMode.HALF_UP);
        BigDecimal totalTrades = BigDecimal.valueOf(this.trades.size());
        BigDecimal daysPerTrade = totalDays.divide(totalTrades, 8, RoundingMode.HALF_UP);

        BigDecimal returnOnTrade = profitPerTrade.divide(BigDecimal.valueOf(100), RoundingMode.FLOOR);
        return returnOnTrade.multiply(BigDecimal.valueOf(365).divide(daysPerTrade, 8, RoundingMode.HALF_UP));
    }

    public BigDecimal getAvgRoi(){
        BigDecimal totalProfit = this.getTotalProfit();
        BigDecimal profitPerTrade = totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, RoundingMode.HALF_UP);

        return profitPerTrade.divide(BigDecimal.valueOf(100), RoundingMode.FLOOR);
    }

    public BigDecimal getAvgProfitPerTrade(){
        BigDecimal totalProfit = this.getTotalProfit();
        return totalProfit.divide(BigDecimal.valueOf(this.trades.size()), 8, BigDecimal.ROUND_HALF_UP);
    }

    public int getWinners(){
        return (int) this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) > 0).count();
    }

    public int getLosers(){
        return (int) this.trades.stream().filter(trade -> trade.getProfit().compareTo(BigDecimal.ZERO) < 0).count();
    }

    public BigDecimal bestTrade(){
        return this.trades
                .stream()
                .map(Trade::getProfit)
                .max(BigDecimal::compareTo)
                .orElseThrow();
    }

    public BigDecimal worstTrade(){
        return this.trades
                .stream()
                .map(Trade::getProfit)
                .min(BigDecimal::compareTo)
                .orElseThrow();
    }

    public BigDecimal avgWinner(){
        if (this.getWinners() == 0){
            return BigDecimal.ZERO;
        }
        return this.trades
                .stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(this.getWinners()), 8, RoundingMode.HALF_UP);
    }

    public BigDecimal avgLoser(){
        if (this.getLosers() == 0){
            return BigDecimal.ZERO;
        }
        return this.trades
                .stream()
                .map(Trade::getProfit)
                .filter(profit -> profit.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(this.getLosers()), 8, RoundingMode.HALF_UP);
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
                "Max winner: " + this.bestTrade() + "\n" +
                "Max loser: " + this.worstTrade() + "\n" +
                "Avg winner: " + this.avgWinner() + "\n" +
                "Avg loser: " + this.avgLoser() + "\n";
    }

}
