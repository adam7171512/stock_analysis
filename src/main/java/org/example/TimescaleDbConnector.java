package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TimescaleDbConnector {

    private static String url = "jdbc:postgresql://localhost:5432/gpw_daily";
    private static String user = "postgres";
    private static String password = "password";

    private TimescaleDbConnector(){
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
