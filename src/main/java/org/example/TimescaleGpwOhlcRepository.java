package org.example;


import org.example.model.Ohlc;
import org.example.model.Timeframe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;


public class TimescaleGpwOhlcRepository implements IOhlcRepository{

    private Connection connection;
    private Map<String, List<Ohlc>> ohlcMap = new HashMap<>();

    public TimescaleGpwOhlcRepository(){
        try {
            connection = TimescaleDbConnector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Ohlc getOhlcAt(Timeframe timeframe, String ticker, LocalDate date) {

        OffsetDateTime offsetDateTime = OffsetDateTime.of(date, LocalTime.of(0, 0), ZoneOffset.UTC);

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM \"" + ticker +"\" WHERE date = ?" // todo: fix leak
                    );

            preparedStatement.setObject(1, offsetDateTime);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println(offsetDateTime);

            if (resultSet.next()) {
                // return Ohlc object
                return new Ohlc(
                        ticker,
                        resultSet.getObject("date", OffsetDateTime.class),
                        resultSet.getBigDecimal("open"),
                        resultSet.getBigDecimal("high"),
                        resultSet.getBigDecimal("low"),
                        resultSet.getBigDecimal("close"),
                        resultSet.getBigDecimal("volume"),
                        resultSet.getBigDecimal("transactions")
                );
            } else {
                // return null
                return null;
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public List<Ohlc> getOhlcFor(Timeframe timeframe, String ticker, LocalDate from, LocalDate to) {

        OffsetDateTime offsetDateTimeFrom = OffsetDateTime.of(from, LocalTime.of(0, 0), ZoneOffset.UTC);
        OffsetDateTime offsetDateTimeTo = OffsetDateTime.of(to, LocalTime.of(0, 0), ZoneOffset.UTC);


        List<Ohlc> ohlcList = new ArrayList<>();

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM \"" + ticker + "\" WHERE date >= ? AND date <= ?" //todo: fix leak
                    );
            preparedStatement.setObject(1, offsetDateTimeFrom);
            preparedStatement.setObject(2, offsetDateTimeTo);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ohlcList.add(
                        new Ohlc(
                                ticker,
                                resultSet.getObject("date", OffsetDateTime.class),
                                resultSet.getBigDecimal("open"),
                                resultSet.getBigDecimal("high"),
                                resultSet.getBigDecimal("low"),
                                resultSet.getBigDecimal("close"),
                                resultSet.getBigDecimal("volume"),
                                resultSet.getBigDecimal("transactions")
                        )
                );
            }

            return ohlcList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private List<Ohlc> readOhlcFromDb(String ticker){


        if (this.ohlcMap.containsKey(ticker)) {
            return this.ohlcMap.get(ticker);
        }
        List<Ohlc> ohlcList = new ArrayList<>();

        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM \"" + ticker + "\""
                    );
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ohlcList.add(
                        new Ohlc(
                                ticker,
                                resultSet.getObject("date", OffsetDateTime.class),
                                resultSet.getBigDecimal("open"),
                                resultSet.getBigDecimal("high"),
                                resultSet.getBigDecimal("low"),
                                resultSet.getBigDecimal("close"),
                                resultSet.getBigDecimal("volume"),
                                resultSet.getBigDecimal("transactions")
                        )
                );
            }

            this.ohlcMap.put(ticker, ohlcList);

            return ohlcList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int buySession, int sellSession, LocalDate date) {
        List<Ohlc> ohlcList = readOhlcFromDb(symbol);
        OffsetDateTime targetDate = OffsetDateTime.of(date, LocalTime.MIDNIGHT, ZoneOffset.UTC);
        int exDivIndex = -1;

        // 1. Iterate over the list and find the index of the ex-dividend date.
        for (int i = 0; i < ohlcList.size(); i++) {
            if (ohlcList.get(i).getDate().isEqual(targetDate)) {
                exDivIndex = i;
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

//    @Override
//    public List<Ohlc> getOhlcAround(Timeframe timeframe, String symbol, int buySession, int sellSession, LocalDate date) {
//        // We need to query db two times: firstly for buySession, secondly for sellSession
//        // We have to sort results by date, and limit them to buySession and sellSession, then join two lists
//
//
//        if (this.ohlcMap.containsKey(symbol)) {
//            return this.ohlcMap.get(symbol);
//        }
//
//        OffsetDateTime offsetDateTime = OffsetDateTime.of(date, LocalTime.of(0, 0), ZoneOffset.UTC);
//
//
//        List<Ohlc> ohlcList = new ArrayList<>();
//
//        try {
//            PreparedStatement preparedStatement =
//                    connection.prepareStatement(
//                            "SELECT * FROM \"" + symbol + "\" WHERE date < ? ORDER BY date DESC LIMIT ?"
//                    );
//            preparedStatement.setObject(1, offsetDateTime);
//            preparedStatement.setInt(2, Math.abs(buySession));
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                ohlcList.add(
//                        new Ohlc(
//                                resultSet.getObject("date", OffsetDateTime.class),
//                                resultSet.getBigDecimal("open"),
//                                resultSet.getBigDecimal("high"),
//                                resultSet.getBigDecimal("low"),
//                                resultSet.getBigDecimal("close"),
//                                resultSet.getBigDecimal("volume"),
//                                resultSet.getBigDecimal("transactions")
//                        )
//                );
//            }
//
//            // sort the list by date ascending
//
//            ohlcList.sort(Comparator.comparing(Ohlc::getDate));
//
//            preparedStatement =
//                    connection.prepareStatement(
//                            "SELECT * FROM \"" + symbol + "\" WHERE date >= ? ORDER BY date ASC LIMIT ?"
//                    );
//            preparedStatement.setObject(1, offsetDateTime);
//            preparedStatement.setInt(2, Math.abs(sellSession) + 1);
//            resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                ohlcList.add(
//                        new Ohlc(
//                                resultSet.getObject("date", OffsetDateTime.class),
//                                resultSet.getBigDecimal("open"),
//                                resultSet.getBigDecimal("high"),
//                                resultSet.getBigDecimal("low"),
//                                resultSet.getBigDecimal("close"),
//                                resultSet.getBigDecimal("volume"),
//                                resultSet.getBigDecimal("transactions")
//                        )
//                );
//            }
//
//
//            if (buySession > 0)
//            {
//                // we leave only last sell - buy elements
//                int idx = sellSession - buySession;
//                ohlcList = ohlcList.subList(idx, ohlcList.size());
//
//            }
//            if (sellSession < 0)
//            {
//                // we leave only first Math.abs(sell - buy) elements
//                ohlcList = ohlcList.subList(0, Math.abs(sellSession - buySession));
//            }
//
//            this.ohlcMap.put(symbol, ohlcList);
//
//            return ohlcList;
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
