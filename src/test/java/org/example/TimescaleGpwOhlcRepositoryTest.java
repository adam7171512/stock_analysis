package org.example;


import org.example.model.Ohlc;
import org.example.model.Timeframe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;


public class TimescaleGpwOhlcRepositoryTest {

    private IOhlcRepository timescaleGpwOhlcRepository;

    @Before
    public void setUp() throws Exception {
        timescaleGpwOhlcRepository = new TimescaleGpwOhlcRepository();
    }


    @Test
    public void getOhlcAt() {
        Ohlc ohlc = timescaleGpwOhlcRepository.getOhlcAt(Timeframe.D1, "PKNORLEN", LocalDate.of(2021, 1, 25));
        Assert.assertEquals(ohlc.getOpen().doubleValue(), 60, 0.001);
    }

    @Test
    public void getOhlcFor() {
        List<Ohlc> ohlcList = timescaleGpwOhlcRepository.getOhlcFor(Timeframe.D1, "PKNORLEN", LocalDate.of(2021, 1, 25), LocalDate.of(2021, 1, 26));
        Assert.assertEquals(ohlcList.size(), 2);
    }

    @Test
    public void getOhlcAround() {
        List<Ohlc> ohlcList = timescaleGpwOhlcRepository.getOhlcAround(Timeframe.D1, "PKNORLEN", 1, 1, LocalDate.of(2021, 1, 25));
        Assert.assertEquals(ohlcList.size(), 3);
        System.out.println(ohlcList);
    }
}