package org.example.model;

import org.example.TickerSetReader;
import org.example.persistence.IDividendRepository;
import org.example.persistence.IOhlcRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class StockPerformanceEvaluatorTest {
    /*
    Please note that those are not real tests, but rather early stage experiments.
    Also note that results do not mimic indices, as the portfolios are not market cap weighted.
     */

    private final IDividendRepository dividendRepository = new FilesystemDividendRepository();
    private final IOhlcRepository ohlcRepository = new GpwFileSystemOhlcRepository();
    private final StockPerformanceEvaluator stockPerformanceEvaluator = new StockPerformanceEvaluator(ohlcRepository, dividendRepository);
    @Test
    public void getAnnualizedRoiForPeriod() {
        BigDecimal PKN_ROI_FOR_2022 = BigDecimal.valueOf(-0.0978);
        BigDecimal roi = stockPerformanceEvaluator.getAnnualizedRoiForPeriod("PKNORLEN", LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1));
        System.out.println(roi);
        assertEquals(PKN_ROI_FOR_2022, roi);
    }

    @Test
    public void getAvgAnnualizedRoiForWig20() throws IOException{
        BigDecimal roi = stockPerformanceEvaluator.getAvgAnnualizedRoiForPeriod(TickerSetReader.getWig20Tickers(), LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1));
        System.out.println(roi);
        assertTrue(roi.compareTo(BigDecimal.valueOf(0.05)) > 0);
        assertTrue(roi.compareTo(BigDecimal.valueOf(0.1)) < 0);
    }
}