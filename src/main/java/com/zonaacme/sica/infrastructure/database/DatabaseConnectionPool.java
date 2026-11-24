package com.zonaacme.sica.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zonaacme.sica.config.AppConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Pool de conexiones centralizado mediante HikariCP (Patrón Singleton).
 * Optimizado para bajo consumo de conexiones simultáneas en MySQL 8.4 (Aiven Cloud).
 */
public final class DatabaseConnectionPool implements AutoCloseable {

    private static volatile DatabaseConnectionPool instance;
    private final HikariDataSource dataSource;

    private DatabaseConnectionPool() {
        AppConfig appConfig = AppConfig.getInstance();

        HikariConfig config = new HikariConfig();
        config.setPoolName("SICA-HikariPool");
        config.setJdbcUrl(appConfig.getDbUrl());
        config.setUsername(appConfig.getDbUser());
        config.setPassword(appConfig.getDbPassword());

        // Parámetros estrictos para entorno compartido en Aiven
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        config.setKeepaliveTime(30000);

        // Optimizaciones recomendadas para MySQL PreparedStatements
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);
    }

    public static DatabaseConnectionPool getInstance() {
        if (instance == null || instance.dataSource.isClosed()) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null || instance.dataSource.isClosed()) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo obtener una conexión del pool HikariCP hacia Aiven MySQL.", e);
        }
    }

    public boolean isHealthy() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
