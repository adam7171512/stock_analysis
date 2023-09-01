package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Deposit {

    private LocalDate date;
    private BigDecimal amount;

    public Deposit(BigDecimal amount, LocalDate date) {
        this.date = date;
        this.amount = amount;
    }

    public LocalDate date() {
        return date;
    }

    public BigDecimal amount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
