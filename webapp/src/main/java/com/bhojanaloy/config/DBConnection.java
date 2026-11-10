package com.bhojanaloy.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection factory, shared by the Swing desktop app and the Spring Boot web demo
 * (webapp/ keeps a byte-identical copy; CI fails if the two drift apart).
 *
 * <p>Each setting is looked up in this order, so no password ever has to be written into the code:
 * <ol>
 *   <li>environment variable {@code DB_URL} / {@code DB_USER} / {@code DB_PASSWORD}</li>
 *   <li>{@code db.properties} in the working directory (keys {@code db.url}, {@code db.user},
 *       {@code db.password}); the file is git-ignored, copy {@code db.properties.example}</li>
 *   <li>the local-dev default below (root with an empty password on localhost)</li>
 * </ol>
 */
public class DBConnection {

    static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/bhojan_aloy?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final Properties FILE = loadFile(Paths.get("db.properties"));

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add mysql-connector-j jar to the build path.");
        }
    }

    private DBConnection() {}

    static Properties loadFile(Path file) {
        Properties p = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream in = new FileInputStream(file.toFile())) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("Could not read " + file + ": " + e.getMessage());
            }
        }
        return p;
    }

    static String setting(String envValue, Properties file, String key, String fallback) {
        if (envValue != null && !envValue.trim().isEmpty()) return envValue;
        String fromFile = file.getProperty(key);
        if (fromFile != null && !fromFile.trim().isEmpty()) return fromFile.trim();
        return fallback;
    }

    public static String url() {
        return setting(System.getenv("DB_URL"), FILE, "db.url", DEFAULT_URL);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url(),
                setting(System.getenv("DB_USER"), FILE, "db.user", "root"),
                setting(System.getenv("DB_PASSWORD"), FILE, "db.password", ""));
    }

    /** Quick sanity check used by the Login screen and GET /api/health. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            return false;
        }
    }
}
