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

public class GpwFileSystemOhlcRepository extends AbstractFileSystemOhlcRepository {
    private final String baseDirectory;
    private Map<String, List<Ohlc>> ohlcMap = new HashMap<>();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");

    public GpwFileSystemOhlcRepository(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public GpwFileSystemOhlcRepository(){
        this.baseDirectory = "src/main/resources/gpw/prices/";
    }

    @Override
    public List<Ohlc> getOhlcList(String ticker) {

        if (this.ohlcMap.containsKey(ticker)) {
            return this.ohlcMap.get(ticker);
        }

        System.out.println("Reading " + ticker + " from file system");

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
            return null;
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        this.ohlcMap.put(ticker, ohlcList);
        return ohlcList;
    }

}

