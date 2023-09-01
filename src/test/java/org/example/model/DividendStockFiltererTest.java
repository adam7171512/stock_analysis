package org.example.model;

import org.example.TickerSetReader;
import org.example.persistence.IDividendRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DividendStockFiltererTest {
    /*
    Please note that those are not real tests, but rather early stage experiments, test functions used as convenience.
     */

    @Test
    public void filterTickersOnDividends() throws IOException {

        DividendStockFilterer dividendStockFilterer = new DividendStockFilterer(
                new FilesystemDividendRepository()
        );
        Set<String> tickers = TickerSetReader.getSWig80Tickers();

        Set<String> filteredTickers = dividendStockFilterer.filterTickersOnDividends(
                tickers,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2024, 1, 1),
                0.03,
                0.23,
                2
        );
        System.out.println(filteredTickers);
        System.out.println(filteredTickers.size());
    }

}