// server/src/main/java/com/hotel/server/util/DatabaseUtil.java
package com.hotel.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL      = ConfigManager.getInstance().getProperty("db.url");
    private static final String USER     = ConfigManager.getInstance().getProperty("db.user");
    private static final String PASSWORD = ConfigManager.getInstance().getProperty("db.password");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        ConfigManager cfg = ConfigManager.getInstance();
        String url = cfg.getProperty("db.url");
        String user = cfg.getProperty("db.user");
        String password = cfg.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
