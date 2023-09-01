package org.example.model;

import org.example.persistence.IDividendRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;

import java.time.LocalDate;
import java.util.*;

public class DividendStockFilterer {

    private IDividendRepository dividendRepository;

    public DividendStockFilterer(){
        this.dividendRepository = new FilesystemDividendRepository();
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
