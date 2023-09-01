package org.example.persistence;

import org.example.model.Ohlc;
import org.example.model.Timeframe;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface IOhlcRepository {

    public Ohlc getOhlcAt(Timeframe timeframe, String symbol, LocalDate time);
    public List<Ohlc> getOhlcFor(Timeframe timeframe, String symbol, LocalDate from, LocalDate to);
    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int before, int after, LocalDate date);
    public Ohlc getClosestOhlc(Timeframe timeframe, String symbol, LocalDate date);
    public Set<LocalDate> getTradingDays(String symbol, LocalDate from, LocalDate to);
}
