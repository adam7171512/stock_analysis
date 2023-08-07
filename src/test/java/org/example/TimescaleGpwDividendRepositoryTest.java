package org.example;


import org.junit.Test;

import java.util.List;

public class TimescaleGpwDividendRepositoryTest {

    @Test
    public void getDividends() {
        TimescaleGpwDividendRepository timescaleGpwDividendRepository = new TimescaleGpwDividendRepository();
        List<model.Dividend> dividendList = (timescaleGpwDividendRepository.getDividends("PKNORLEN"));
        System.out.println(dividendList);

    }

    public void testGetDividends() {
    }

    public void testGetDividends1() {
    }

    @Test
    public void testGetTickers() {
        TimescaleGpwDividendRepository timescaleGpwDividendRepository = new TimescaleGpwDividendRepository();
        List<String> tickers = timescaleGpwDividendRepository.getTickers();
        System.out.println(tickers);
    }
}