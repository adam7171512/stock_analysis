package org.example.persistence.timeScale;

import org.example.model.BinanceTransactionData;
import org.example.model.Ohlc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimeScaleBinanceRepository {

    private Connection connection;

    public TimeScaleBinanceRepository() {
        try {
            connection = TimescaleDbConnector.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BinanceTransactionData> getAllTransactions() {

        List<BinanceTransactionData> transactionDataList = new LinkedList<>();
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(
                            "SELECT * FROM transactions"
                    );
            var timePrepStart = System.currentTimeMillis();
            ResultSet resultSet = preparedStatement.executeQuery();

            var timeStart = System.currentTimeMillis();
            while (resultSet.next()) {
                transactionDataList.add(
                        new BinanceTransactionData(
                                resultSet.getLong("id"),
                                resultSet.getDouble("price"),
                                resultSet.getDouble("qty"),
                                resultSet.getDouble("base_qty"),
                                resultSet.getObject("time", OffsetDateTime.class),
                                resultSet.getBoolean("is_buyer_maker")
                        )
                );
            }
            var timeEnd = System.currentTimeMillis();
            System.out.println("Prep time: " + (timePrepStart - timeStart) + "ms");
            System.out.println("Time: " + (timeEnd - timeStart) + "ms");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return transactionDataList;
    }
}
