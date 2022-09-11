package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    public static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:pp.db");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS games (_id INTEGER PRIMARY KEY, name TEXT UNIQUE, channels TEXT, roles TEXT, enabled BOOLEAN)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
