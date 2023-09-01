package org.example.persistence;

import org.example.model.Ohlc;
import org.example.model.StockPriceInfoNearDate;
import org.example.model.StockPriceInfoNearDividend;
import org.example.model.Timeframe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractFileSystemOhlcRepository implements IOhlcRepository{
    private final String baseDirectory;
    private Map<String, List<Ohlc>> ohlcMap = new HashMap<>();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");

    public AbstractFileSystemOhlcRepository(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public AbstractFileSystemOhlcRepository(){
        this.baseDirectory = "src/main/resources/us/prices/";
    }

    @Override
    public Ohlc getOhlcAt(Timeframe timeframe, String ticker, LocalDate date) {
        List<Ohlc> ohlcList = getOhlcList(ticker);

        if (ohlcList == null) {
            return null;
        }


        OffsetDateTime targetDate = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        return ohlcList.stream()
                .filter(ohlc -> ohlc.getDate().isEqual(targetDate))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Ohlc> getOhlcFor(Timeframe timeframe, String ticker, LocalDate from, LocalDate to) {
        List<Ohlc> ohlcList = getOhlcList(ticker);

        if (ohlcList == null) {
            return null;
        }

        OffsetDateTime fromDate = OffsetDateTime.of(from, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(to, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        return ohlcList.stream()
                .filter(ohlc -> !ohlc.getDate().isBefore(fromDate) && !ohlc.getDate().isAfter(toDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int buySession, int sellSession, LocalDate date) {
        List<Ohlc> ohlcList = getOhlcList(symbol);

        if (ohlcList == null) {
            return null;
        }

        OffsetDateTime targetDate = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        int exDivIndex = -1;

        // 1. Iterate over the list and find the index of the ex-dividend date.
        for (int i = 0; i < ohlcList.size(); i++) {
            if (ohlcList.get(i).getDate().isEqual(targetDate)) {
                exDivIndex = i;
                if (ohlcList.get(i).getVolume().compareTo(BigDecimal.ZERO) == 0) {
                    return Collections.emptyList();
                }
                break;
            }
        }

        // Check if exDivIndex is found, otherwise return an empty list
        if (exDivIndex == -1) {
            return Collections.emptyList();
        }

        // 2. Depending on buySession and sellSession, select the sublist.
        int startIndex = Math.max(0, exDivIndex + buySession);
        int endIndex = Math.min(ohlcList.size(), exDivIndex + sellSession + 1);

        return ohlcList.subList(startIndex, endIndex);
    }

    public StockPriceInfoNearDate getStockPriceInfoNearDate(String ticker, LocalDate date, int sessionsBefore, int sessionsAfter) {
        List<Ohlc> ohlcList = getOhlcAround(Timeframe.D1, ticker, sessionsBefore, sessionsAfter, date);
        System.out.println(ohlcList);
        //create map with int from sesionbefore to sessionafter
        Map<Integer, Ohlc> ohlcMap = new HashMap<>();
        int a = 0;
        for (int i = sessionsBefore; i <= sessionsAfter; i++) {
            ohlcMap.put(i, ohlcList.get(a++));
        }
        return new StockPriceInfoNearDate(ticker, ohlcMap);
    }

    public StockPriceInfoNearDividend getStockPriceInfoNearDividend(model.Dividend dividend, int sessionsBefore, int sessionsAfter){
        List<Ohlc> ohlcList = getOhlcAround(Timeframe.D1, dividend.getName(), sessionsBefore, sessionsAfter, dividend.getExDate());

        Map<Integer, Ohlc> ohlcMap = new HashMap<>();
        int a = 0;
        for (int i = sessionsBefore; i <= sessionsAfter; i++) {
            ohlcMap.put(i, ohlcList.get(a++));
        }
        return new StockPriceInfoNearDividend(ohlcMap, dividend);
    }

    public abstract List<Ohlc> getOhlcList(String ticker);

    public List<String> getCompanies(String filePath) throws IOException {
        List<String> result = new ArrayList<>();
        File file = new File(filePath);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split(" ", 2);
            if (splitLine.length > 0 && !splitLine[0].isEmpty()) {
                result.add(splitLine[0]);
            }
        }
        br.close();

        return result;
    }

    public Ohlc getClosestOhlc(Timeframe timeFrame, String ticker, LocalDate date){
        Ohlc ohlc = getOhlcAt(timeFrame, ticker, date);
        if (ohlc == null){
            int counter = 1;
            while (ohlc == null && counter < 30){
                LocalDate dateToCheck = date.minusDays(counter);
                ohlc = getOhlcAt(timeFrame, ticker, dateToCheck);
                counter++;
            }
        }
        return ohlc;
    }

    @Override
    public Set<LocalDate> getTradingDays(String symbol, LocalDate from, LocalDate to) {
        List<Ohlc> ohlcList = getOhlcList(symbol);

        if (ohlcList == null) {
            return null;
        }

        return ohlcList.stream()
                .map(Ohlc::getDate)
                .map(OffsetDateTime::toLocalDate)
                .filter(date -> date.isAfter(from) && date.isBefore(to))
                .collect(Collectors.toSet());
    }
}
