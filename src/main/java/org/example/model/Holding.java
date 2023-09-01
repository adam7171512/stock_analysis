package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Holding(String ticker, BigDecimal size, BigDecimal price) {
    public BigDecimal getCurrentValue(BigDecimal currentPrice) {
        return size.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal getCost() {
        return size.multiply(price).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Holding holding)) return false;
        return ticker.equals(holding.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker);
    }
}
