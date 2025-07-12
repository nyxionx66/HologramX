package com.hologramx;

import com.hologramx.api.HologramXAPI;
import com.hologramx.commands.HologramCommand;
import com.hologramx.commands.HologramUserCommand;
import com.hologramx.config.ConfigManager;
import com.hologramx.config.Messages;
import com.hologramx.holograms.HologramManager;
import com.hologramx.listeners.PlayerListener;
import com.hologramx.listeners.ChunkListener;
import com.hologramx.storage.StorageManager;
import com.hologramx.utils.PlaceholderManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HologramX extends JavaPlugin {

    private static HologramX instance;
    private static HologramXAPI api;
    
    private ConfigManager configManager;
    private Messages messages;
    private StorageManager storageManager;
    private HologramManager hologramManager;
    private PlaceholderManager placeholderManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        messages = new Messages(this);
        
        // Initialize storage
        storageManager = new StorageManager(this);
        if (!storageManager.initialize()) {
            getLogger().severe("Failed to initialize storage! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize hologram manager
        hologramManager = new HologramManager(this);
        
        // Initialize placeholder manager
        placeholderManager = new PlaceholderManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Load holograms
        hologramManager.loadHolograms();
        
        // Initialize API
        api = new HologramXAPI(this);
        
        // Start metrics
        new Metrics(this, 19847);
        
        getLogger().info("HologramX v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.saveHolograms();
            hologramManager.removeAllHolograms();
        }
        
        if (storageManager != null) {
            storageManager.close();
        }
        
        getLogger().info("HologramX has been disabled!");
    }
    
    private void registerCommands() {
        HologramCommand hologramCommand = new HologramCommand(this);
        HologramUserCommand userCommand = new HologramUserCommand(this);
        
        getCommand("hologramx").setExecutor(hologramCommand);
        getCommand("hologramx").setTabCompleter(hologramCommand);
        
        getCommand("hologram").setExecutor(userCommand);
        getCommand("hologram").setTabCompleter(userCommand);
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
    }
    
    public void reload() {
        // Save current holograms
        hologramManager.saveHolograms();
        hologramManager.removeAllHolograms();
        
        // Reload configuration
        configManager.reload();
        messages.reload();
        
        // Reload holograms
        hologramManager.loadHolograms();
        
        getLogger().info("HologramX has been reloaded!");
    }
    
    public static HologramX getInstance() {
        return instance;
    }
    
    public static HologramXAPI getAPI() {
        return api;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public Messages getMessages() {
        return messages;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}