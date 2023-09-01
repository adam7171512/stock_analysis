package org.example.persistence.fileSystem;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import model.Dividend;
import org.example.persistence.IDividendRepository;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesystemDividendRepository implements IDividendRepository {

    private static final String DIVIDENDS_PATH = "src/main/resources/gpw/dividends.csv";

    @Override
    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to) {
        return getDividends(ticker, from, to, 0.0, 1.0);
    }

    @Override
    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to, double minYield, double maxYield) {
        return readDividendsFromFile().stream().filter(dividend -> {
            if (dividend.getName().equals(ticker) && dividend.getExDate().isAfter(from) && dividend.getExDate().isBefore(to)) {
                double yield = dividend.getYield().doubleValue();
                return yield >= minYield && yield <= maxYield;
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Dividend> getDividends(String ticker) {
        return getDividends(ticker, LocalDate.MIN, LocalDate.MAX);
    }

    @Override
    public List<String> getTickers() {
        return readDividendsFromFile().stream().map(Dividend::getName).distinct().collect(Collectors.toList());
    }

    private List<Dividend> readDividendsFromFile(){
        List<Dividend> dividendList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(DIVIDENDS_PATH);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {

                LocalDate exDate = LocalDate.parse(nextRecord[3]);
                LocalDate declarationDate;
                try {
                    declarationDate = LocalDate.parse(nextRecord[4]);
                }
                catch (Exception e){
                    declarationDate = exDate;
                }

                dividendList.add(new Dividend(
                        nextRecord[0],
                        new BigDecimal(nextRecord[1]),
                        new BigDecimal(nextRecord[2]),
                        exDate,
                        declarationDate
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from CSV file.", e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // merge dividends with the same ex date , sum their amounts and yield, also filter out 0.00 amount dividends

        List<Dividend> mergedDividends = new ArrayList<>();

        for (Dividend dividend : dividendList) {
            if (dividend.getAmount().doubleValue() == 0.00) {
                continue;
            }

            if (mergedDividends.isEmpty()) {
                mergedDividends.add(dividend);
            } else {
                Dividend lastDividend = mergedDividends.get(mergedDividends.size() - 1);
                if (lastDividend.getExDate().equals(dividend.getExDate())) {
                    lastDividend.setAmount(lastDividend.getAmount().add(dividend.getAmount()));
                    lastDividend.setYield(lastDividend.getYield().add(dividend.getYield()));
                } else {
                    mergedDividends.add(dividend);
                }
            }
        }



        return mergedDividends;
    }
}
