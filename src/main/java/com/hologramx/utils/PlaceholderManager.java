package com.hologramx.utils;

import com.hologramx.HologramX;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderManager {
    
    private final HologramX plugin;
    private final boolean placeholderAPIEnabled;
    
    public PlaceholderManager(HologramX plugin) {
        this.plugin = plugin;
        this.placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        
        if (placeholderAPIEnabled) {
            plugin.getLogger().info("PlaceholderAPI found! Placeholder support enabled.");
        } else {
            plugin.getLogger().info("PlaceholderAPI not found. Placeholder support disabled.");
        }
    }
    
    public boolean isEnabled() {
        return placeholderAPIEnabled && plugin.getConfigManager().isPlaceholdersEnabled();
    }
    
    public String setPlaceholders(Player player, String text) {
        if (!isEnabled()) return text;
        
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            plugin.getLogger().warning("Error setting placeholders: " + e.getMessage());
            return text;
        }
    }
    
    public boolean containsPlaceholders(String text) {
        return text != null && text.contains("%");
    }
}