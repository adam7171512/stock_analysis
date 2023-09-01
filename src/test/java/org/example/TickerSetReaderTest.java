package org.example;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TickerSetReaderTest {

    @Test
    public void getSWig80Tickers() throws IOException {
        Set<String> tickers = TickerSetReader.getSWig80Tickers();
        assertEquals(80, tickers.size());
    }

    @Test
    public void getMWig40Tickers() throws IOException {
        Set<String> tickers = TickerSetReader.getMWig40Tickers();
        assertEquals(40, tickers.size());

    }

    @Test
    public void getWig20Tickers() throws IOException {
        Set<String> tickers = TickerSetReader.getWig20Tickers();
        assertEquals(20, tickers.size());
    }
}