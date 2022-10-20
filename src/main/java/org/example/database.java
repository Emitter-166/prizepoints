package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    public static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:pp.db");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS games (_id INTEGER PRIMARY KEY, name TEXT UNIQUE, " +
                    "channels TEXT DEFAULT '908893077886861342 974482860650135592 890586473735258172 937515500961943614 880087479603056691 " +
                    "910430606779883541 870201769869844480 980059079869362197 931757641552764998 892747389671178260 896271426301096008 926314036201652264 " +
                    "933551584175067196 ', roles TEXT DEFAULT '989472251298586654 989472515065794570 994938573298085928 '," +
                    " createPoints INTEGER DEFAULT 3, messagePoints INTEGER DEFAULT 5, enabled BOOLEAN DEFAULT false)");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS threads (_id INTEGER UNIQUE, points INTEGER)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
