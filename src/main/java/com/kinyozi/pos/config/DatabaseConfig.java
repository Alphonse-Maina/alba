package com.kinyozi.pos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
    private static Properties dbProps = new Properties();

    static {
        loadProperties();
        initPool();
    }

    private static void loadProperties() {
        try (InputStream is = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is != null) {
                dbProps.load(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties: " + e.getMessage());
        }
    }

    private static void initPool() {
        try {
            HikariConfig config = new HikariConfig();
            String host = dbProps.getProperty("db.host", "localhost");
            String port = dbProps.getProperty("db.port", "5432");
            String name = dbProps.getProperty("db.name", "kinyozi_pos");
            String user = dbProps.getProperty("db.username", "postgres");
            String pass = dbProps.getProperty("db.password", "");

            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + name);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMinimumIdle(2);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setPoolName("KinyoziPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("DB pool init failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not configured. Check db.properties.");
        }
        return dataSource.getConnection();
    }

    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    public static void updateConnection(String host, String port, String db, String user, String pass) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        dbProps.setProperty("db.host", host);
        dbProps.setProperty("db.port", port);
        dbProps.setProperty("db.name", db);
        dbProps.setProperty("db.username", user);
        dbProps.setProperty("db.password", pass);
        initPool();
    }
}