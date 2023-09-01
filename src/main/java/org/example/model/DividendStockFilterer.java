package org.example.model;

import org.example.persistence.IDividendRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;

import java.time.LocalDate;
import java.util.*;

public class DividendStockFilterer {

    private final IDividendRepository dividendRepository;

    public DividendStockFilterer(
            IDividendRepository dividendRepository
    ){
        this.dividendRepository = dividendRepository;
    }

    public Set<String> filterTickersOnDividends(Set<String> tickers, LocalDate from, LocalDate to, double minYield, double maxYield, int minOccurrences){
        Set<String> filteredTickers = new HashSet<>();
        for(String ticker : tickers){
            List<model.Dividend> dividends = dividendRepository.getDividends(ticker, from, to, minYield, maxYield);
            if(dividends.size() >= minOccurrences){
                filteredTickers.add(ticker);
            }
        }
        return filteredTickers;
    }

}
