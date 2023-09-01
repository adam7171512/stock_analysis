package org.example.persistence.fileSystem;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.StockPriceInfoNearDate;
import org.example.model.StockPriceInfoNearDividend;
import org.example.persistence.AbstractFileSystemOhlcRepository;
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

public class UsFileSystemOhlcRepository extends AbstractFileSystemOhlcRepository {
    private final String baseDirectory;
    private Map<String, List<Ohlc>> ohlcMap = new HashMap<>();
    ZoneOffset zoneOffset = ZoneOffset.UTC;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UsFileSystemOhlcRepository(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public UsFileSystemOhlcRepository(){
        this.baseDirectory = "src/main/resources/us/prices/";
    }

    @Override
    public List<Ohlc> getOhlcList(String ticker) {

        if (this.ohlcMap.containsKey(ticker)) {
            return this.ohlcMap.get(ticker);
        }

        List<Ohlc> ohlcList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(baseDirectory + "/" + ticker + ".csv");
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

            String[] nextRecord;


            while ((nextRecord = csvReader.readNext()) != null) {
                LocalDate localDate = LocalDate.parse(nextRecord[1], DATE_TIME_FORMATTER);
                OffsetDateTime d = OffsetDateTime.of(localDate, LocalTime.of(0, 0), zoneOffset);

                ohlcList.add(new Ohlc(
                        ticker,
                        d,
                        new BigDecimal(nextRecord[2]),
                        new BigDecimal(nextRecord[3]),
                        new BigDecimal(nextRecord[4]),
                        new BigDecimal(nextRecord[5]),
                        new BigDecimal(nextRecord[6]),
                        new BigDecimal(nextRecord[7])
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

}

