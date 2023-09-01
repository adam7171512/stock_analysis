package org.example.persistence;

import model.Dividend;

import java.time.LocalDate;
import java.util.List;

public interface IDividendRepository {

    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to);
    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to, double minYield, double maxYield);
    public List<Dividend> getDividends(String ticker);
    public List<String> getTickers();
}
