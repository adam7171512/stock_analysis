package org.example.model;

import org.checkerframework.checker.units.qual.A;
import org.example.TickerSetReader;
import org.example.persistence.IDividendRepository;
import org.example.persistence.IOhlcRepository;
import org.example.persistence.PortfolioAnalysisDTO;
import org.example.persistence.PortfolioRunResultToDTOConverter;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;
import org.example.sender.StockDataSender;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class PortfolioStrategyAnalysisTest {

    private IOhlcRepository ohlcRepository;
    private IDividendRepository dividendRepository;

    @Before
    public void setUp(){
        this.ohlcRepository = new GpwFileSystemOhlcRepository();
        this.dividendRepository = new FilesystemDividendRepository();
    }

    @Test
    public void analyse() throws IOException {

        Set<String> tickers = TickerSetReader.getMWig40Tickers();
//        tickers.addAll(TickerSetReader.getWig20Tickers());
//        tickers.addAll(TickerSetReader.getMWig40Tickers());
        DividendStockFilterer dividendStockFilterer = new DividendStockFilterer();
        Set<String> dividendTickers = dividendStockFilterer.filterTickersOnDividends(tickers, LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 3), 0.015, 0.9, 2);
//        tickers.removeAll(dividendTickers);


        LocalDate startDate = LocalDate.of(2018, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 5, 1);

        PortfolioStrategyAnalysis allTickersAnalysis =
                new PortfolioStrategyAnalysis(
                        dividendTickers,
                        0,
                        0,
                        dividendRepository,
                        ohlcRepository
                        );
        PortfolioRunResult snapshots = allTickersAnalysis.analyse(
                            startDate,
                            endDate,
                            BigDecimal.valueOf(10000)
        );

        PortfolioStrategyAnalysis dividendTickersAnalysis =
                new PortfolioStrategyAnalysis(
                        dividendTickers,
                        0,
                        120,
                        dividendRepository,
                        ohlcRepository
                );
        PortfolioRunResult dividendSnapshots = dividendTickersAnalysis.analyse(
                startDate,
                endDate,
                BigDecimal.valueOf(10000)
        );



        System.out.println(snapshots);
        System.out.println(snapshots.getInvestmentValueOverTime());
//        System.out.println(dividendTickers.size());

        System.out.println(snapshots.getStartingHoldings());

        StockDataSender stockDataSender = new StockDataSender();

        PortfolioRunResultToDTOConverter converter = new PortfolioRunResultToDTOConverter();
        PortfolioAnalysisDTO dto = converter.convert(snapshots);

        List<PortfolioAnalysisDTO> dtos = new ArrayList<>();
        dtos.add(converter.convert(snapshots));
        dtos.add(converter.convert(dividendSnapshots));


        stockDataSender.sendMultipleStrategyAnalysisDataToFlask(dtos);
    }
}