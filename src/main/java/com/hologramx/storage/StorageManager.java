package com.hologramx.storage;

import com.hologramx.HologramX;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StorageManager {
    
    private final HologramX plugin;
    private HikariDataSource dataSource;
    private boolean usingDatabase = false;
    
    public StorageManager(HologramX plugin) {
        this.plugin = plugin;
    }
    
    public boolean initialize() {
        String type = plugin.getConfigManager().getDatabaseType().toUpperCase();
        
        switch (type) {
            case "MYSQL" -> {
                return initializeMySQL();
            }
            case "SQLITE" -> {
                return initializeSQLite();
            }
            default -> {
                plugin.getLogger().info("Using YAML storage.");
                return true;
            }
        }
    }
    
    private boolean initializeMySQL() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + plugin.getConfigManager().getMysqlHost() + 
                             ":" + plugin.getConfigManager().getMysqlPort() + 
                             "/" + plugin.getConfigManager().getMysqlDatabase());
            config.setUsername(plugin.getConfigManager().getMysqlUsername());
            config.setPassword(plugin.getConfigManager().getMysqlPassword());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                plugin.getLogger().info("Successfully connected to MySQL database.");
            }
            
            createTables();
            usingDatabase = true;
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize MySQL connection: " + e.getMessage());
            return false;
        }
    }
    
    private boolean initializeSQLite() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/holograms.db");
            config.setDriverClassName("org.sqlite.JDBC");
            
            // SQLite specific settings
            config.setMaximumPoolSize(1);
            config.setConnectionTimeout(30000);
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                plugin.getLogger().info("Successfully connected to SQLite database.");
            }
            
            createTables();
            usingDatabase = true;
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite connection: " + e.getMessage());
            return false;
        }
    }
    
    private void createTables() throws SQLException {
        String createHologramsTable = """
            CREATE TABLE IF NOT EXISTS holograms (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                type VARCHAR(20) NOT NULL,
                world VARCHAR(50) NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw FLOAT DEFAULT 0,
                pitch FLOAT DEFAULT 0,
                data TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(createHologramsTable)) {
            statement.executeUpdate();
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }
    
    public boolean isUsingDatabase() {
        return usingDatabase;
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}