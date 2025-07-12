package com.hologramx.api;

import com.hologramx.HologramX;
import com.hologramx.holograms.Hologram;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

/**
 * HologramX API for developers
 * Provides programmatic access to hologram management
 */
public class HologramXAPI {
    
    private final HologramX plugin;
    
    public HologramXAPI(HologramX plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Create a new text hologram
     * @param id Unique identifier for the hologram
     * @param location Location where the hologram should be spawned
     * @return The created hologram, or null if a hologram with this ID already exists
     */
    public Hologram createTextHologram(String id, Location location) {
        return plugin.getHologramManager().createHologram(id, Hologram.HologramType.TEXT, location);
    }
    
    /**
     * Create a new item hologram
     * @param id Unique identifier for the hologram
     * @param location Location where the hologram should be spawned
     * @return The created hologram, or null if a hologram with this ID already exists
     */
    public Hologram createItemHologram(String id, Location location) {
        return plugin.getHologramManager().createHologram(id, Hologram.HologramType.ITEM, location);
    }
    
    /**
     * Create a new block hologram
     * @param id Unique identifier for the hologram
     * @param location Location where the hologram should be spawned
     * @return The created hologram, or null if a hologram with this ID already exists
     */
    public Hologram createBlockHologram(String id, Location location) {
        return plugin.getHologramManager().createHologram(id, Hologram.HologramType.BLOCK, location);
    }
    
    /**
     * Get a hologram by its ID
     * @param id The hologram ID
     * @return The hologram, or null if not found
     */
    public Hologram getHologram(String id) {
        return plugin.getHologramManager().getHologram(id);
    }
    
    /**
     * Delete a hologram
     * @param id The hologram ID
     * @return true if the hologram was deleted, false if not found
     */
    public boolean deleteHologram(String id) {
        return plugin.getHologramManager().deleteHologram(id);
    }
    
    /**
     * Get all holograms
     * @return Collection of all holograms
     */
    public Collection<Hologram> getAllHolograms() {
        return plugin.getHologramManager().getHolograms();
    }
    
    /**
     * Get holograms near a location
     * @param location The center location
     * @param radius The search radius
     * @return List of nearby holograms
     */
    public List<Hologram> getNearbyHolograms(Location location, double radius) {
        return plugin.getHologramManager().getNearbyHolograms(location, radius);
    }
    
    /**
     * Check if a hologram exists
     * @param id The hologram ID
     * @return true if the hologram exists
     */
    public boolean hologramExists(String id) {
        return plugin.getHologramManager().getHologram(id) != null;
    }
    
    /**
     * Save all holograms to storage
     */
    public void saveHolograms() {
        plugin.getHologramManager().saveHolograms();
    }
    
    /**
     * Reload all holograms from storage
     */
    public void reloadHolograms() {
        plugin.getHologramManager().removeAllHolograms();
        plugin.getHologramManager().loadHolograms();
    }
}