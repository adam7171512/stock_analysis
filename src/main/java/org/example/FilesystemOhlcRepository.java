package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Ohlc;
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

public class FilesystemOhlcRepository implements IOhlcRepository {
    private final String baseDirectory;
    private Map<String, List<Ohlc>> ohlcMap = new HashMap<>();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");

    public FilesystemOhlcRepository(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public Ohlc getOhlcAt(Timeframe timeframe, String ticker, LocalDate date) {
        List<Ohlc> ohlcList = readOhlcFromFile(ticker);
        OffsetDateTime targetDate = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        return ohlcList.stream()
                .filter(ohlc -> ohlc.getDate().isEqual(targetDate))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Ohlc> getOhlcFor(Timeframe timeframe, String ticker, LocalDate from, LocalDate to) {
        List<Ohlc> ohlcList = readOhlcFromFile(ticker);
        OffsetDateTime fromDate = OffsetDateTime.of(from, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(to, LocalTime.MIDNIGHT, ZoneOffset.UTC);

        return ohlcList.stream()
                .filter(ohlc -> !ohlc.getDate().isBefore(fromDate) && !ohlc.getDate().isAfter(toDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int buySession, int sellSession, LocalDate date) {
        List<Ohlc> ohlcList = readOhlcFromFile(symbol);
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


    private List<Ohlc> readOhlcFromFile(String ticker) {

        if (this.ohlcMap.containsKey(ticker)) {
            return this.ohlcMap.get(ticker);
        }

        List<Ohlc> ohlcList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(baseDirectory + "/" + ticker + ".csv");
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                ohlcList.add(new Ohlc(
                        ticker,
                        OffsetDateTime.parse(nextRecord[0], DATE_TIME_FORMATTER),
                        new BigDecimal(nextRecord[1]),
                        new BigDecimal(nextRecord[2]),
                        new BigDecimal(nextRecord[3]),
                        new BigDecimal(nextRecord[4]),
                        new BigDecimal(nextRecord[5]),
                        new BigDecimal(nextRecord[6])
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from CSV file.", e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        this.ohlcMap.put(ticker, ohlcList);
        return ohlcList;
    }

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
}
