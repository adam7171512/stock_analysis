package org.example;

import model.Dividend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimescaleGpwDividendRepository implements IDividendRepository{

    private Connection connection;

    public TimescaleGpwDividendRepository(){
        try {
            connection = TimescaleDbConnector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to) {
        OffsetDateTime offsetDateTimeFrom = OffsetDateTime.of(from, LocalTime.of(0, 0), ZoneOffset.UTC);
        OffsetDateTime offsetDateTimeTo = OffsetDateTime.of(to, LocalTime.of(0, 0), ZoneOffset.UTC);

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM dividends WHERE name = ? AND ex_div_date >= ? AND ex_div_date <= ?"
                    );

            preparedStatement.setString(1, ticker );
            preparedStatement.setObject(2, from);
            preparedStatement.setObject(3, to);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Dividend> dividends = new ArrayList<>();


            while (resultSet.next()) {
                // return Ohlc object
                Dividend newDividend = new Dividend(
                        resultSet.getObject("name", String.class),
                        resultSet.getBigDecimal("amount"),
                        resultSet.getBigDecimal("yield"),
                        resultSet.getObject("ex_div_date", LocalDate.class),
                        resultSet.getObject("decision_date", LocalDate.class)
                );
                dividends.add(newDividend);
            }

            dividends.sort(Comparator.comparing(Dividend::getExDate));

            // merge dividends with the same ex date , sum their amounts and yield, also filter out 0.00 amount dividends

            List<Dividend> mergedDividends = new ArrayList<>();

            for (Dividend dividend : dividends) {
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

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Dividend> getDividends(String ticker, LocalDate from, LocalDate to, double minYield, double maxYield) {
        List<Dividend> datedDividends = getDividends(ticker, from, to);
        List<Dividend> filteredDividends = new ArrayList<>();

        for (Dividend dividend : datedDividends) {
            if (dividend.getYield().doubleValue() >= minYield && dividend.getYield().doubleValue() <= maxYield) {
                filteredDividends.add(dividend);
            }
        }

        return filteredDividends;
    }

    @Override
    public List<Dividend> getDividends(String ticker) {
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.now();
        return getDividends(ticker, from, to);
    }

    @Override
    public List<String> getTickers() {
        // return unique dividend payers from table dividends

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT DISTINCT name FROM dividends"
                    );

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> tickers = new ArrayList<>();

            while (resultSet.next()) {
                tickers.add(resultSet.getString("name"));
            }

            return tickers;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}