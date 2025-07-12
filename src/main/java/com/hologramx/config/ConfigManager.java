package com.hologramx.config;

import com.hologramx.HologramX;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final HologramX plugin;
    private FileConfiguration config;
    
    public ConfigManager(HologramX plugin) {
        this.plugin = plugin;
        load();
    }
    
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reload() {
        load();
    }
    
    // Database settings
    public String getDatabaseType() {
        return config.getString("database.type", "YAML");
    }
    
    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "hologramx");
    }
    
    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "password");
    }
    
    // Performance settings
    public int getViewDistance() {
        return config.getInt("performance.view-distance", 64);
    }
    
    public int getUpdateInterval() {
        return config.getInt("performance.update-interval", 20);
    }
    
    public boolean isChunkLoadingEnabled() {
        return config.getBoolean("performance.chunk-loading", true);
    }
    
    public boolean isAsyncOperationsEnabled() {
        return config.getBoolean("performance.async-operations", true);
    }
    
    public int getMaxHologramsPerChunk() {
        return config.getInt("performance.max-per-chunk", 50);
    }
    
    // Default settings
    public String getDefaultBillboard() {
        return config.getString("defaults.billboard", "vertical");
    }
    
    public String getDefaultTextAlignment() {
        return config.getString("defaults.text-alignment", "center");
    }
    
    public double getDefaultScaleX() {
        return config.getDouble("defaults.scale.x", 1.0);
    }
    
    public double getDefaultScaleY() {
        return config.getDouble("defaults.scale.y", 1.0);
    }
    
    public double getDefaultScaleZ() {
        return config.getDouble("defaults.scale.z", 1.0);
    }
    
    public double getDefaultTranslationX() {
        return config.getDouble("defaults.translation.x", 0.0);
    }
    
    public double getDefaultTranslationY() {
        return config.getDouble("defaults.translation.y", 0.0);
    }
    
    public double getDefaultTranslationZ() {
        return config.getDouble("defaults.translation.z", 0.0);
    }
    
    public double getDefaultShadowRadius() {
        return config.getDouble("defaults.shadow.radius", 0.0);
    }
    
    public double getDefaultShadowStrength() {
        return config.getDouble("defaults.shadow.strength", 1.0);
    }
    
    public int getDefaultVisibilityDistance() {
        return config.getInt("defaults.visibility.distance", -1);
    }
    
    public String getDefaultVisibilityType() {
        return config.getString("defaults.visibility.type", "ALL");
    }
    
    public String getDefaultBackground() {
        return config.getString("defaults.background", "transparent");
    }
    
    public boolean getDefaultTextShadow() {
        return config.getBoolean("defaults.text-shadow", false);
    }
    
    public boolean getDefaultSeeThrough() {
        return config.getBoolean("defaults.see-through", false);
    }
    
    // Placeholder settings
    public boolean isPlaceholdersEnabled() {
        return config.getBoolean("placeholders.enabled", true);
    }
    
    public int getPlaceholderUpdateInterval() {
        return config.getInt("placeholders.update-interval", 20);
    }
    
    public boolean isPlaceholderCacheEnabled() {
        return config.getBoolean("placeholders.cache-enabled", true);
    }
    
    public int getPlaceholderCacheDuration() {
        return config.getInt("placeholders.cache-duration", 30);
    }
    
    // Animation settings
    public boolean isAnimationsEnabled() {
        return config.getBoolean("animations.enabled", true);
    }
    
    public int getMaxAnimationsPerHologram() {
        return config.getInt("animations.max-per-hologram", 5);
    }
    
    public int getAnimationUpdateRate() {
        return config.getInt("animations.update-rate", 2);
    }
    
    // Backup settings
    public boolean isBackupEnabled() {
        return config.getBoolean("backup.enabled", true);
    }
    
    public int getBackupInterval() {
        return config.getInt("backup.interval", 24);
    }
    
    public int getMaxBackupFiles() {
        return config.getInt("backup.max-files", 7);
    }
    
    // Debug settings
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
    
    public boolean isPerformanceLoggingEnabled() {
        return config.getBoolean("debug.performance-logging", false);
    }
}