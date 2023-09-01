package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Dividend {

    private String name;
    private BigDecimal amount;
    private BigDecimal yield;
    private LocalDate exDate;
    private LocalDate decisionDate;

    public Dividend(String name, BigDecimal amount, BigDecimal yield, LocalDate exDate, LocalDate decisionDate) {
        this.name = name;
        this.amount = amount;
        this.yield = yield;
        this.exDate = exDate;
        this.decisionDate = decisionDate;
    }

    public Dividend(String name, BigDecimal amount, LocalDate exDate, LocalDate decisionDate) {
        this.amount = amount;
        this.exDate = exDate;
        this.decisionDate = decisionDate;
    }

    public Dividend(String name, BigDecimal amount, LocalDate exDate) {
        this.amount = amount;
        this.exDate = exDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExDate() {
        return exDate;
    }

    @Override
    public String toString() {
        return "Dividend{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", yield=" + yield +
                ", exDate=" + exDate +
                ", decisionDate=" + decisionDate +
                '}';
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public BigDecimal getYield() {
        return yield;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setYield(BigDecimal yield) {
        this.yield = yield;
    }

    public String getName() {
        return name;
    }
}
