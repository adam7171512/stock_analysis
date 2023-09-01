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


    private final IDividendRepository dividendRepository = new FilesystemDividendRepository();
    private final IOhlcRepository ohlcRepository = new GpwFileSystemOhlcRepository();
    private StockPerformanceEvaluator stockPerformanceEvaluator = new StockPerformanceEvaluator(ohlcRepository, dividendRepository);

    @Test
    public void getAnnualizedRoiForPeriod() {
        BigDecimal PKN_ROI_FOR_2022 = BigDecimal.valueOf(-0.1028);
        BigDecimal roi = stockPerformanceEvaluator.getAnnualizedRoiForPeriod("PKNORLEN", LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1));
        assertEquals(PKN_ROI_FOR_2022, roi);
    }

    @Test
    public void getAvgAnnualizedRoiForPeriod() throws IOException {
        BigDecimal roi = stockPerformanceEvaluator.getAvgAnnualizedRoiForPeriod(TickerSetReader.getWig20Tickers(), LocalDate.of(2019, 1, 1), LocalDate.of(2023, 1, 1));
        System.out.println(roi);
        BigDecimal roi2 = stockPerformanceEvaluator.getAvgAnnualizedRoiForPeriod(TickerSetReader.getSWig80Tickers(), LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1));
        System.out.println(roi2);
        DividendStockFilterer dividendStockFilterer = new DividendStockFilterer();
        Set<String> filteredStocks = dividendStockFilterer.filterTickersOnDividends(TickerSetReader.getSWig80Tickers(), LocalDate.of(2016, 1, 1), LocalDate.of(2019, 1, 1), 0.02, 0.23, 2);
        BigDecimal roiFiltered = stockPerformanceEvaluator.getAvgAnnualizedRoiForPeriod(filteredStocks, LocalDate.of(2021, 1, 1), LocalDate.of(2023, 1, 1));
        System.out.println(roiFiltered);
    }
}