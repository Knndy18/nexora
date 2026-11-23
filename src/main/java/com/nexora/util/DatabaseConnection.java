package com.nexora.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexión SQLite.
 * Proporciona conexiones con PRAGMA foreign_keys = ON
 * y soporte para transacciones atómicas (ACID).
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:sqlite:nexora.db";

    private DatabaseConnection() {}

    public static synchronized void setDatabaseUrl(String newUrl) {
        dbUrl = newUrl;
    }

    public static synchronized String getDatabaseUrl() {
        return dbUrl;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void closeConnection() {
        // Hook de cierre limpio
    }
}
