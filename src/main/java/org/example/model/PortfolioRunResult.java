package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortfolioRunResult {

    private Map<LocalDate, PortfolioSnapshot> snapshotMap;

    public PortfolioRunResult(Map<LocalDate, PortfolioSnapshot> snapshotMap){
        this.snapshotMap = snapshotMap;
    }

    public List<Holding> getStartingHoldings(){
        return getFirstSnapshot().getHoldingsList();
    }

    public List<Holding> getEndingHoldings(){
        return getLastSnapshot().getHoldingsList();
    }

    public PortfolioSnapshot getFirstSnapshot(){
        return snapshotMap.get(snapshotMap.keySet().stream().sorted().findFirst().orElseThrow());
    }

    public PortfolioSnapshot getLastSnapshot(){
        return snapshotMap.get(snapshotMap.keySet().stream().sorted().reduce((first, second) -> second).orElseThrow());
    }

    public List<PortfolioSnapshot> getSnapshotList(){
        return snapshotMap.values().stream().sorted().collect(Collectors.toList());
    }

    public LocalDate getStartDate(){
        return getFirstSnapshot().getDate();
    }

    public LocalDate getEndDate(){
        return getLastSnapshot().getDate();
    }

    public long getDays(){
        return getEndDate().toEpochDay() - getStartDate().toEpochDay();
    }

    public BigDecimal getStartingValue(){
        return getFirstSnapshot().getValueAtSnapshot();
    }

    public BigDecimal getEndingValue(){
        return getLastSnapshot().getValueAtSnapshot();
    }

    public BigDecimal getProfit(){
        return getEndingValue().subtract(getStartingValue());
    }

    public BigDecimal getProfitPercent(){
        return getProfit().divide(getStartingValue(), 4, RoundingMode.FLOOR).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getAnnualizedProfitPercent(){
        return getProfitPercent().multiply(BigDecimal.valueOf(365)).divide(BigDecimal.valueOf(getDays()), 4, RoundingMode.FLOOR);
    }

    public BigDecimal getStartingCash(){
        return getFirstSnapshot().getCash();
    }

    public BigDecimal getEndingCash(){
        return getLastSnapshot().getCash();
    }

    public Map<LocalDate, BigDecimal> getInvestmentValueOverTime(){
        return snapshotMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValueAtSnapshot()));
    }

    public BigDecimal getAvgUnInvestedRatio(){
        return snapshotMap.values().stream().map(PortfolioSnapshot::getUnInvestedRatio).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(snapshotMap.size()), 4, RoundingMode.FLOOR);
    }

    @Override
    public String toString() {
        return "PortfolioRunResult{" +
                "profit=" + getProfit() +
                ", profitPercent=" + getProfitPercent() +
                ", annualizedProfitPercent=" + getAnnualizedProfitPercent() +
                ", startingCash=" + getStartingCash() +
                ", endingCash=" + getEndingCash() +
                ", startingValue=" + getStartingValue() +
                ", endingValue=" + getEndingValue() +
                ", days=" + getDays() +
                ", startDate=" + getStartDate() +
                ", endDate=" + getEndDate() +
                ", avgUnInvestedRatio=" + getAvgUnInvestedRatio() +
                '}';
    }
}
