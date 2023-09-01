package org.example.model;

import java.math.BigDecimal;

public class FeeManager {
    public static BigDecimal getBrokerFee() {
        return new BigDecimal("0.002");
    }

    public static BigDecimal getDividendTax() {
        return new BigDecimal("0.19");
    }

    public static BigDecimal getSlippage() {
        return new BigDecimal("0.005");
    }
}
