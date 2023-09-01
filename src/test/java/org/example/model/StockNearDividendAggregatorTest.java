package org.example.model;

import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;

import org.example.sender.StockDataSender;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockNearDividendAggregatorTest {

    /*
    Please note that those are not real tests, but rather early stage experiments, test functions used as convenience.
     */

    @Test
    public void createStockNearDividendInfoGraph() {
        FilesystemDividendRepository filesystemDividendRepository = new FilesystemDividendRepository();
        GpwFileSystemOhlcRepository filesystemOhlcRepository = new GpwFileSystemOhlcRepository();


        List<String> dividendPayers = filesystemDividendRepository.getTickers();
        List<model.Dividend> dividends = new ArrayList<>();
        StockNearDividendAggregator stockNearDividendAggregator = new StockNearDividendAggregator();


        for (String dividendPayer : dividendPayers) {
            dividends.addAll(filesystemDividendRepository.getDividends(
                    dividendPayer,
                    LocalDate.of(2010, 1, 1),
                    LocalDate.of(2022, 1, 1)
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
}