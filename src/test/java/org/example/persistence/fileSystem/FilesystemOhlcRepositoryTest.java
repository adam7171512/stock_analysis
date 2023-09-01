package org.example.persistence.fileSystem;

import org.example.model.Ohlc;
import org.example.model.StockPriceInfoNearDate;
import org.example.model.StockPriceInfoNearDividend;
import org.example.model.Timeframe;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class FilesystemOhlcRepositoryTest {

    @Test
    public void getStockPriceInfoNearDate() {
        GpwFileSystemOhlcRepository filesystemOhlcRepository = new GpwFileSystemOhlcRepository();
        System.out.println();

        StockPriceInfoNearDate stockPriceInfoNearDateList = filesystemOhlcRepository.getStockPriceInfoNearDate(
                "PKNORLEN",                LocalDate.of(2020, 1, 8)
                , -2, 1
        );

        assertEquals(3 , stockPriceInfoNearDateList.getDailyReturnMap().values().size());


    }

    @Test
    public void getStockPriceInfoNearDividend() {
        FilesystemDividendRepository filesystemDividendRepository = new FilesystemDividendRepository();
        GpwFileSystemOhlcRepository filesystemOhlcRepository = new GpwFileSystemOhlcRepository();

        List<model.Dividend> dividends = filesystemDividendRepository.getDividends("PKNORLEN");
        model.Dividend div = dividends.get(0);

        StockPriceInfoNearDividend stockPriceInfoNearDividend = filesystemOhlcRepository.getStockPriceInfoNearDividend(
                div,
                 -8,
                3
        );
        System.out.println(stockPriceInfoNearDividend.getDailyReturnMap());
        System.out.println(stockPriceInfoNearDividend.getDailyReturnMapAdjustedForDividend());
    }
}