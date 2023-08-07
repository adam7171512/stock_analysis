package org.example.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Ohlc {
    private OffsetDateTime date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal transactionCount;

    public Ohlc(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, OffsetDateTime date) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public Ohlc(OffsetDateTime date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal volume, BigDecimal transactionCount) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.transactionCount = transactionCount;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getTransactionCount() {
        return transactionCount;
    }

    @Override
    public String toString() {
        return "Ohlc{" +
                "date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", transactionCount=" + transactionCount +
                '}';
    }
}
