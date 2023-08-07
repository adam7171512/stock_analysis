package org.example;

import org.example.model.Ohlc;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

public class Trade {

    private String symbol;
    private Ohlc entryOhlc;
    private Ohlc exitOhlc;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal size;
    private BigDecimal brokerFee = FeeManager.getBrokerFee();
    private BigDecimal dividendTax = FeeManager.getDividendTax();
    private BigDecimal slippage = FeeManager.getSlippage();
    private model.Dividend dividend;

    public Trade(Ohlc entryOhlc, Ohlc exitOhlc, BigDecimal entryPrice, BigDecimal exitPrice, BigDecimal size) {
        this.entryOhlc = entryOhlc;
        this.exitOhlc = exitOhlc;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.size = size;
    }

    public void setDividend(model.Dividend dividend) {
        this.dividend = dividend;
    }

    public model.Dividend getDividend() {
        return dividend;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setEntryOhlc(Ohlc entryOhlc) {
        this.entryOhlc = entryOhlc;
    }

    public Ohlc getEntryOhlc() {
        return entryOhlc;
    }

    public void setExitOhlc(Ohlc exitOhlc) {
        this.exitOhlc = exitOhlc;
    }

    public Ohlc getExitOhlc() {
        return exitOhlc;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setBrokerFee(BigDecimal brokerFee) {
        this.brokerFee = brokerFee;
    }

    public BigDecimal getBrokerFee() {
        return brokerFee;
    }

    public BigDecimal getProfit() {
        return getTradingProfit().add(getDividendProfit());
    }

    public BigDecimal getTradingFee(){
        return entryPrice.multiply(size).multiply(brokerFee).add(exitPrice.multiply(size).multiply(brokerFee));
    }

    public BigDecimal getTradingProfit(){
        return size.multiply(exitPrice.subtract(entryPrice)).subtract(getTradingFee());
    }

    public BigDecimal getDividendProfit(){
        BigDecimal totalProfit = BigDecimal.ZERO;
        if (this.dividend != null){
            totalProfit = totalProfit.add(this.dividend.getAmount().multiply(size).multiply(BigDecimal.ONE.subtract(dividendTax)));
        }
        return totalProfit;
    }

    public BigDecimal getProfitPct() {
        return getProfit().divide(entryPrice.multiply(size), 8, BigDecimal.ROUND_HALF_UP);
    }

    public long getDays(){
        return entryOhlc.getDate().until(exitOhlc.getDate(), ChronoUnit.DAYS);
    }

    public String getStats(){
        return "Size " + size +  " Entry: " + entryOhlc.getDate() + " " + entryPrice + " Exit: " + exitOhlc.getDate() + " " + exitPrice +
                " Profit: " + getProfit() + " ProfitPct: " + getProfitPct() + "Trading profit : " + getTradingProfit() + " Dividend profit: " + getDividendProfit() + " Trading fee: " + getTradingFee();
    }

    @Override
    public String toString(){
        return getStats();
    }

}
