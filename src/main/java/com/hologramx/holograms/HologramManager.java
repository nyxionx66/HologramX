package com.hologramx.holograms;

import com.hologramx.HologramX;
import com.hologramx.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {
    
    private final HologramX plugin;
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
    private final Map<String, Set<Hologram>> chunkHolograms = new ConcurrentHashMap<>();
    private final File hologramsFile;
    private FileConfiguration hologramsConfig;
    
    public HologramManager(HologramX plugin) {
        this.plugin = plugin;
        this.hologramsFile = new File(plugin.getDataFolder(), "holograms.yml");
        loadConfig();
        startUpdateTask();
    }
    
    private void loadConfig() {
        if (!hologramsFile.exists()) {
            plugin.saveResource("holograms.yml", false);
        }
        hologramsConfig = YamlConfiguration.loadConfiguration(hologramsFile);
    }
    
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateHolograms();
            }
        }.runTaskTimer(plugin, 20L, plugin.getConfigManager().getUpdateInterval());
    }
    
    public void loadHolograms() {
        plugin.getLogger().info("Loading holograms...");
        
        ConfigurationSection section = hologramsConfig.getConfigurationSection("");
        if (section == null) {
            plugin.getLogger().info("No holograms found to load.");
            return;
        }
        
        for (String id : section.getKeys(false)) {
            try {
                Hologram hologram = loadHologram(id, section.getConfigurationSection(id));
                if (hologram != null) {
                    holograms.put(id, hologram);
                    addToChunkIndex(hologram);
                    hologram.spawn();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load hologram '" + id + "': " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + holograms.size() + " holograms.");
    }
    
    private Hologram loadHologram(String id, ConfigurationSection section) {
        if (section == null) return null;
        
        Hologram hologram = new Hologram(id);
        
        // Load type
        String typeStr = section.getString("type", "TEXT");
        try {
            hologram.setType(Hologram.HologramType.valueOf(typeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid hologram type '" + typeStr + "' for hologram '" + id + "'");
            return null;
        }
        
        // Load location
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        if (locationSection != null) {
            String worldName = locationSection.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for hologram '" + id + "'");
                return null;
            }
            
            double x = locationSection.getDouble("x");
            double y = locationSection.getDouble("y");
            double z = locationSection.getDouble("z");
            float yaw = (float) locationSection.getDouble("yaw", 0);
            float pitch = (float) locationSection.getDouble("pitch", 0);
            
            Location location = new Location(world, x, y, z, yaw, pitch);
            hologram.setLocation(location);
        }
        
        // Load visibility settings
        hologram.setVisibilityDistance(section.getInt("visibility_distance", -1));
        String visibilityStr = section.getString("visibility", "ALL");
        try {
            hologram.setVisibility(Hologram.VisibilityType.valueOf(visibilityStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            hologram.setVisibility(Hologram.VisibilityType.ALL);
        }
        
        // Load other properties
        hologram.setPersistent(section.getBoolean("persistent", true));
        hologram.setScaleX((float) section.getDouble("scale_x", 1.0));
        hologram.setScaleY((float) section.getDouble("scale_y", 1.0));
        hologram.setScaleZ((float) section.getDouble("scale_z", 1.0));
        hologram.setTranslationX((float) section.getDouble("translation_x", 0.0));
        hologram.setTranslationY((float) section.getDouble("translation_y", 0.0));
        hologram.setTranslationZ((float) section.getDouble("translation_z", 0.0));
        hologram.setShadowRadius((float) section.getDouble("shadow_radius", 0.0));
        hologram.setShadowStrength((float) section.getDouble("shadow_strength", 1.0));
        
        // Load text-specific properties
        if (hologram.getType() == Hologram.HologramType.TEXT) {
            List<String> textLines = section.getStringList("text");
            hologram.setTextLines(textLines);
            hologram.setTextShadow(section.getBoolean("text_shadow", false));
            hologram.setSeeThrough(section.getBoolean("see_through", false));
            
            String alignmentStr = section.getString("text_alignment", "CENTER");
            try {
                hologram.setTextAlignment(Hologram.TextAlignment.valueOf(alignmentStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                hologram.setTextAlignment(Hologram.TextAlignment.CENTER);
            }
            
            hologram.setUpdateTextInterval(section.getInt("update_text_interval", -1));
            hologram.setBackground(section.getString("background", "transparent"));
        }
        
        // Load billboard
        String billboardStr = section.getString("billboard", "VERTICAL");
        try {
            hologram.setBillboard(Hologram.BillboardType.valueOf(billboardStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            hologram.setBillboard(Hologram.BillboardType.VERTICAL);
        }
        
        return hologram;
    }
    
    public void saveHolograms() {
        plugin.getLogger().info("Saving holograms...");
        
        hologramsConfig = new YamlConfiguration();
        
        for (Hologram hologram : holograms.values()) {
            if (hologram.isPersistent()) {
                saveHologram(hologram);
            }
        }
        
        try {
            hologramsConfig.save(hologramsFile);
            plugin.getLogger().info("Saved " + holograms.size() + " holograms.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save holograms: " + e.getMessage());
        }
    }
    
    private void saveHologram(Hologram hologram) {
        String id = hologram.getId();
        
        // Save type
        hologramsConfig.set(id + ".type", hologram.getType().name());
        
        // Save location
        Location loc = hologram.getLocation();
        if (loc != null) {
            hologramsConfig.set(id + ".location.world", loc.getWorld().getName());
            hologramsConfig.set(id + ".location.x", loc.getX());
            hologramsConfig.set(id + ".location.y", loc.getY());
            hologramsConfig.set(id + ".location.z", loc.getZ());
            hologramsConfig.set(id + ".location.yaw", loc.getYaw());
            hologramsConfig.set(id + ".location.pitch", loc.getPitch());
        }
        
        // Save visibility settings
        hologramsConfig.set(id + ".visibility_distance", hologram.getVisibilityDistance());
        hologramsConfig.set(id + ".visibility", hologram.getVisibility().name());
        
        // Save other properties
        hologramsConfig.set(id + ".persistent", hologram.isPersistent());
        hologramsConfig.set(id + ".scale_x", hologram.getScaleX());
        hologramsConfig.set(id + ".scale_y", hologram.getScaleY());
        hologramsConfig.set(id + ".scale_z", hologram.getScaleZ());
        hologramsConfig.set(id + ".translation_x", hologram.getTranslationX());
        hologramsConfig.set(id + ".translation_y", hologram.getTranslationY());
        hologramsConfig.set(id + ".translation_z", hologram.getTranslationZ());
        hologramsConfig.set(id + ".shadow_radius", hologram.getShadowRadius());
        hologramsConfig.set(id + ".shadow_strength", hologram.getShadowStrength());
        
        // Save text-specific properties
        if (hologram.getType() == Hologram.HologramType.TEXT) {
            hologramsConfig.set(id + ".text", hologram.getTextLines());
            hologramsConfig.set(id + ".text_shadow", hologram.isTextShadow());
            hologramsConfig.set(id + ".see_through", hologram.isSeeThrough());
            hologramsConfig.set(id + ".text_alignment", hologram.getTextAlignment().name());
            hologramsConfig.set(id + ".update_text_interval", hologram.getUpdateTextInterval());
            hologramsConfig.set(id + ".background", hologram.getBackground());
        }
        
        // Save billboard
        hologramsConfig.set(id + ".billboard", hologram.getBillboard().name());
    }
    
    public Hologram createHologram(String id, Hologram.HologramType type, Location location) {
        if (holograms.containsKey(id)) {
            return null;
        }
        
        Hologram hologram = new Hologram(id);
        hologram.setType(type);
        hologram.setLocation(location);
        
        holograms.put(id, hologram);
        addToChunkIndex(hologram);
        hologram.spawn();
        
        return hologram;
    }
    
    public boolean deleteHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            hologram.despawn();
            removeFromChunkIndex(hologram);
            return true;
        }
        return false;
    }
    
    public Hologram getHologram(String id) {
        return holograms.get(id);
    }
    
    public Collection<Hologram> getHolograms() {
        return holograms.values();
    }
    
    public List<Hologram> getHologramsInChunk(Chunk chunk) {
        String chunkKey = getChunkKey(chunk);
        Set<Hologram> chunkHolos = chunkHolograms.get(chunkKey);
        return chunkHolos != null ? new ArrayList<>(chunkHolos) : new ArrayList<>();
    }
    
    public void removeAllHolograms() {
        holograms.values().forEach(Hologram::despawn);
        holograms.clear();
        chunkHolograms.clear();
    }
    
    private void updateHolograms() {
        for (Hologram hologram : holograms.values()) {
            if (hologram.isLoaded() && hologram.needsUpdate()) {
                hologram.updateText();
            }
        }
    }
    
    public void updateHologramsForPlayer(Player player) {
        for (Hologram hologram : holograms.values()) {
            if (hologram.isLoaded()) {
                hologram.updateForPlayer(player);
            }
        }
    }
    
    public void onChunkLoad(Chunk chunk) {
        List<Hologram> chunkHolos = getHologramsInChunk(chunk);
        for (Hologram hologram : chunkHolos) {
            if (!hologram.isLoaded()) {
                hologram.spawn();
            }
        }
    }
    
    public void onChunkUnload(Chunk chunk) {
        List<Hologram> chunkHolos = getHologramsInChunk(chunk);
        for (Hologram hologram : chunkHolos) {
            if (hologram.isLoaded()) {
                hologram.despawn();
            }
        }
    }
    
    private void addToChunkIndex(Hologram hologram) {
        Location loc = hologram.getLocation();
        if (loc != null) {
            String chunkKey = getChunkKey(loc.getChunk());
            chunkHolograms.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(hologram);
        }
    }
    
    private void removeFromChunkIndex(Hologram hologram) {
        Location loc = hologram.getLocation();
        if (loc != null) {
            String chunkKey = getChunkKey(loc.getChunk());
            Set<Hologram> chunkHolos = chunkHolograms.get(chunkKey);
            if (chunkHolos != null) {
                chunkHolos.remove(hologram);
                if (chunkHolos.isEmpty()) {
                    chunkHolograms.remove(chunkKey);
                }
            }
        }
    }
    
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    public List<Hologram> getNearbyHolograms(Location location, double radius) {
        List<Hologram> nearby = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            if (hologram.getLocation() != null && 
                hologram.getLocation().getWorld().equals(location.getWorld()) &&
                hologram.getLocation().distance(location) <= radius) {
                nearby.add(hologram);
            }
        }
        return nearby;
    }
}