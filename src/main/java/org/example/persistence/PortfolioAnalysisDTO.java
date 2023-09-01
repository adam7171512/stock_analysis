package org.example.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PortfolioAnalysisDTO {

    private String analysisDate;
    private String startingCapital;
    private String totalProfit;
    private String startDate;
    private String endDate;
    private String roiPercentage;
    private String yearlyAdjustedRoiPercentage;
    private String averageCashBalance;
    private List<SnapshotDTO> snapshots;

    public PortfolioAnalysisDTO() {
    }

    public PortfolioAnalysisDTO(String analysisDate, String startingCapital, String totalProfit, String startDate, String endDate, String roiPercentage, String yearlyAdjustedRoiPercentage, String averageCashBalance, List<SnapshotDTO> snapshots) {
        this.analysisDate = analysisDate;
        this.startingCapital = startingCapital;
        this.totalProfit = totalProfit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roiPercentage = roiPercentage;
        this.yearlyAdjustedRoiPercentage = yearlyAdjustedRoiPercentage;
        this.averageCashBalance = averageCashBalance;
        this.snapshots = snapshots;
    }

    // Getters and setters
    public String getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(String analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getStartingCapital() {
        return startingCapital;
    }

    public void setStartingCapital(String startingCapital) {
        this.startingCapital = startingCapital;
    }

    public String getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(String totalProfit) {
        this.totalProfit = totalProfit;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRoiPercentage() {
        return roiPercentage;
    }

    public void setRoiPercentage(String roiPercentage) {
        this.roiPercentage = roiPercentage;
    }

    public String getYearlyAdjustedRoiPercentage() {
        return yearlyAdjustedRoiPercentage;
    }

    public void setYearlyAdjustedRoiPercentage(String yearlyAdjustedRoiPercentage) {
        this.yearlyAdjustedRoiPercentage = yearlyAdjustedRoiPercentage;
    }

    public String getAverageCashBalance() {
        return averageCashBalance;
    }

    public void setAverageCashBalance(String averageCashBalance) {
        this.averageCashBalance = averageCashBalance;
    }

    public List<SnapshotDTO> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<SnapshotDTO> snapshots) {
        this.snapshots = snapshots;
    }
}

class SnapshotDTO {

    private String date;
    private List<HoldingDTO> holdings;
    private String cash;
    private String value;

    public SnapshotDTO() {
    }

    public SnapshotDTO(String date, List<HoldingDTO> holdings, String cash, String value) {
        this.date = date;
        this.holdings = holdings;
        this.cash = cash;
        this.value = value;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<HoldingDTO> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingDTO> holdings) {
        this.holdings = holdings;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class HoldingDTO {

    private String name;
    private String size;
    private String price;

    public HoldingDTO() {
    }

    public HoldingDTO(String name, String size, String price) {
        this.name = name;
        this.size = size;
        this.price = price;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
