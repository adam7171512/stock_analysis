package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FreeCashManager {

    private static final BigDecimal INTEREST_RATE = BigDecimal.valueOf(0.04);
    private List<Deposit> depositList;

    private BigDecimal deposits = BigDecimal.ZERO;
    private BigDecimal withdrawals = BigDecimal.ZERO;

    public FreeCashManager(){
        this.depositList = new ArrayList<>();
    }

    public void deposit(BigDecimal amount, LocalDate date){
        this.deposits = this.deposits.add(amount);
        this.depositList.add(new Deposit(amount, date));
        System.out.println("Deposited " + amount + " on " + date);
    }

    public BigDecimal getCurrentBalance(LocalDate date){
        BigDecimal amount = BigDecimal.ZERO;
        for(Deposit deposit : depositList){
            if(deposit.date().isEqual(date)){
                amount = amount.add(getDepositAmountWithInterest(deposit, date));
            } else if (deposit.date().isBefore(date)) {
                // calculate interest rate
                amount = amount.add(getDepositAmountWithInterest(deposit, date));
            }
        }
        return amount;
    }

    public BigDecimal withdraw(BigDecimal amount, LocalDate date){
        BigDecimal currentBalance = getCurrentBalance(date);
//        if(currentBalance.compareTo(amount) < 0){
//            throw new IllegalArgumentException("Not enough money");
//        }
        System.out.println("Withdrawing " + amount);
        System.out.println("balance before : " + getCurrentBalance(date));
        // take money from the oldest deposit

        Iterator<Deposit> iterator = depositList.iterator();
        BigDecimal amountTaken = BigDecimal.ZERO;

        while (iterator.hasNext()) {
            Deposit deposit = iterator.next();
            if (!deposit.date().isAfter(date)) {
                if (getDepositAmountWithInterest(deposit, date).compareTo(amount.subtract(amountTaken)) >= 0) {
                    // take money from this deposit
                    BigDecimal totalValueOfDeposit = getDepositAmountWithInterest(deposit, date);
                    BigDecimal depositWithoutInterest = deposit.amount();
                    BigDecimal interests = totalValueOfDeposit.subtract(depositWithoutInterest);

                    // calculate ratio of total amount to be taken
                    BigDecimal ratio = (amount.subtract(amountTaken)).divide(totalValueOfDeposit, 2, RoundingMode.FLOOR);

                    // take money from deposit
                    BigDecimal toDeduct = depositWithoutInterest.multiply(ratio);

                    System.out.println("Taking " + toDeduct + " from deposit " + deposit.date());

                    deposit.setAmount(deposit.amount().subtract(toDeduct));
                    break;
                } else {
                    BigDecimal amountPlusInterests = getDepositAmountWithInterest(deposit, date);
                    amountTaken = amountTaken.add(amountPlusInterests);

                    System.out.println("Taking " + amountPlusInterests + " from deposit " + deposit.date());
                    iterator.remove();
                }
            }
        }
        System.out.println("Withdrawn " + amount);
        System.out.println("balance after : " + getCurrentBalance(date));
        this.withdrawals = this.withdrawals.add(amount);

        System.out.println("Deposits: " + this.deposits);
        System.out.println("Withdrawals: " + this.withdrawals);
        return amount;
    }

    private BigDecimal getDepositAmountWithInterest(Deposit deposit, LocalDate date) {
        long days = ChronoUnit.DAYS.between(deposit.date(), date);
        BigDecimal interest = deposit.amount().multiply(INTEREST_RATE).multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(365), 2, RoundingMode.FLOOR);
        return deposit.amount().add(interest);
    }

}
