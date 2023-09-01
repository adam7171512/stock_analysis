package org.example;

import org.example.persistence.IDividendRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class FilesystemDividendRepositoryTest {

    @Test
    public void getTickers() {
        IDividendRepository dividendRepository = new FilesystemDividendRepository();
        assertTrue(dividendRepository.getTickers().contains("PKNORLEN"));
    }


    @Test
    public void getDividends() {
        IDividendRepository dividendRepository = new FilesystemDividendRepository();
        List<model.Dividend> dividends = dividendRepository.getDividends(
                "PKNORLEN",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 1),
                0.0
                , 0.1
        );
        assertEquals(1, dividends.size());
    }
    }