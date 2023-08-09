package org.example.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Ohlc {
    private String ticker;
    private OffsetDateTime date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    private BigDecimal volume;
    private BigDecimal difference;


    public Ohlc(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, OffsetDateTime date) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public Ohlc(String ticker, OffsetDateTime date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal difference, BigDecimal volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.ticker = ticker;
        this.difference = difference;

        if (this.volume.compareTo(BigDecimal.ZERO) < 0){
            throw new RuntimeException("Volume cant be lower than 0!" + this.ticker + this.volume + this.date + this.open + this.close);
        }

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

    public BigDecimal getDifference() {
        return difference;
    }

    public String getTicker(){
        return this.ticker;
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
                ", difference=" + difference +
                '}';
    }
}
