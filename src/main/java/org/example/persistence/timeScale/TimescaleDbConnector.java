package org.example.persistence.timeScale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TimescaleDbConnector {

    private static String url = "jdbc:postgresql://46.171.250.14:5432/binance";
    private static String user = "postgres";
    private static String password = "tajniak90";

    private TimescaleDbConnector(){
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }


}
