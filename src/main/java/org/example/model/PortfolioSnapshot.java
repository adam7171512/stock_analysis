package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PortfolioSnapshot implements Comparable<PortfolioSnapshot> {

    private LocalDate date;
    private Set<Holding> holdings;
    private BigDecimal cash;

    public PortfolioSnapshot(LocalDate date, Set<Holding> holdings, BigDecimal cash) {
        this.date = date;
        this.holdings = holdings;
        this.cash = cash;
    }

    public LocalDate getDate() {
        return date;
    }

    public Set<Holding> getHoldings() {
        return new HashSet<>(holdings);
    }

    public BigDecimal getCash() {
        return cash.setScale(2, RoundingMode.FLOOR);
    }

    public BigDecimal getCurrentValueOfAssets(Map<String, BigDecimal> currentPrices){
        BigDecimal value = BigDecimal.ZERO;
        for (Holding holding: holdings){
            value = value.add(holding.getCurrentValue(currentPrices.get(holding.ticker())));
        }
        return value;
    }

    public BigDecimal getValueAtSnapshot(){
        BigDecimal value = BigDecimal.ZERO;
        for (Holding holding: holdings){
            value = value.add(holding.getCost());
        }
        return value.add(cash);
    }

    public List<Holding> getHoldingsList() {
        //compare tickers and sort
        return holdings.stream().sorted((h1, h2) -> h1.ticker().compareTo(h2.ticker())).collect(Collectors.toList());
    }

    public BigDecimal getUnInvestedRatio(){
        return getCash().divide(getValueAtSnapshot(), 4, RoundingMode.FLOOR);
    }

    @Override
    public String toString() {
        return "PortfolioSnapshot :{" +
                "date=" + date +
                ", holdings=" + getHoldingsList() +
                ", cash=" + cash +
                " value at snapshot=" + getValueAtSnapshot().setScale(2, RoundingMode.FLOOR);

    }

    @Override
    public int compareTo(PortfolioSnapshot portfolioSnapshot) {
        return this.date.compareTo(portfolioSnapshot.date);
    }
}
