package org.example.model;

import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;

import org.example.persistence.fileSystem.UsFileSystemOhlcRepository;
import org.example.sender.StockDataSender;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StockNearDividendAggregatorTest {

    @Test
    public void add() {
        FilesystemDividendRepository filesystemDividendRepository = new FilesystemDividendRepository();
        UsFileSystemOhlcRepository filesystemOhlcRepository = new UsFileSystemOhlcRepository();


        List<String> dividendPayers = filesystemDividendRepository.getTickers();
        List<model.Dividend> dividends = new ArrayList<>();
        StockNearDividendAggregator stockNearDividendAggregator = new StockNearDividendAggregator();


        for (String dividendPayer : dividendPayers) {
            dividends.addAll(filesystemDividendRepository.getDividends(
                    dividendPayer,
                    LocalDate.of(1980, 1, 1),
                    LocalDate.of(2010, 1, 1)
//                    0.02,
//                    0.06
                    ));
        }

        for (model.Dividend dividend: dividends) {
            try {
                StockPriceInfoNearDividend stockPriceInfoNearDividend = filesystemOhlcRepository.getStockPriceInfoNearDividend(
                        dividend,
                        -50,
                        50
                );
                stockNearDividendAggregator.add(stockPriceInfoNearDividend);
            }
            catch (Exception e) {
                System.out.println("Exception: " + e);
            }
        }


        System.out.println(stockNearDividendAggregator.getAggregationCount());
        StockDataSender stockDataSender = new StockDataSender();
        stockDataSender.sendToFlask(stockNearDividendAggregator.getAvgDailyReturnsDividendAdjusted(), stockNearDividendAggregator.getAvgDividendYield());

    }

    @Test
    public void getAvgDailyReturns() {
    }

    @Test
    public void getAvgDividendYield() {
    }
}