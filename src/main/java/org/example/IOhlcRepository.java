package org.example;

import org.example.model.Ohlc;
import org.example.model.Timeframe;

import java.time.LocalDate;
import java.util.List;

public interface IOhlcRepository {

    public Ohlc getOhlcAt(Timeframe timeframe, String symbol, LocalDate time);
    public List<Ohlc> getOhlcFor(Timeframe timeframe, String symbol, LocalDate from, LocalDate to);
    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int before, int after, LocalDate date);
}
