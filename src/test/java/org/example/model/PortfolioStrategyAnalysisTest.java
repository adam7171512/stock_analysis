package org.example.model;

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
import java.util.*;

public class PortfolioStrategyAnalysisTest {
    /*
    Please note that those are not real tests, but rather early stage experiments, test functions used as convenience.
     */

    private IOhlcRepository ohlcRepository;
    private IDividendRepository dividendRepository;

    @Before
    public void setUp(){
        this.ohlcRepository = new GpwFileSystemOhlcRepository();
        this.dividendRepository = new FilesystemDividendRepository();
    }

    @Test
    public void createPortolioStrategyComparisonGraph() throws IOException {

        Set<String> wig20Tickers = TickerSetReader.getMWig40Tickers();
        Set<String> mwig40Tickers = TickerSetReader.getMWig40Tickers();
        Set<String> swig80Tickers = TickerSetReader.getSWig80Tickers();
        Set<String> tickers = new HashSet<>();
        tickers.addAll(wig20Tickers);
        tickers.addAll(mwig40Tickers);
        tickers.addAll(swig80Tickers);

        // Filter tickers to get those matching our criteria for dividends
        DividendStockFilterer dividendStockFilterer = new DividendStockFilterer(
                new FilesystemDividendRepository()
        );
        Set<String> dividendTickers = dividendStockFilterer
                .filterTickersOnDividends(
                        tickers,
                        LocalDate.of(2016, 1, 1),
                        LocalDate.of(2018, 1, 3),
                        0.015,
                        0.9,
                        2
                );

        // Set start and end date for analysis
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
        System.out.println(dividendTickers.size());

        System.out.println(snapshots.getStartingHoldings());

        StockDataSender stockDataSender = new StockDataSender();

        PortfolioRunResultToDTOConverter converter = new PortfolioRunResultToDTOConverter();

        List<PortfolioAnalysisDTO> dtos = new ArrayList<>();
        dtos.add(converter.convert(snapshots));
        dtos.add(converter.convert(dividendSnapshots));

        stockDataSender.sendMultipleStrategyAnalysisDataToFlask(dtos);
    }
}