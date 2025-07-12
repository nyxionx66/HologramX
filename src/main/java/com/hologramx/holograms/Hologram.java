package com.hologramx.holograms;

import com.hologramx.HologramX;
import com.hologramx.utils.LocationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.*;

public class Hologram {
    
    private final String id;
    private HologramType type;
    private Location location;
    private int visibilityDistance;
    private VisibilityType visibility;
    private boolean persistent;
    private float scaleX, scaleY, scaleZ;
    private float translationX, translationY, translationZ;
    private float shadowRadius, shadowStrength;
    private List<String> textLines;
    private boolean textShadow;
    private boolean seeThrough;
    private TextAlignment textAlignment;
    private int updateTextInterval;
    private String background;
    private BillboardType billboard;
    
    // Runtime data
    private final Set<UUID> viewers = new HashSet<>();
    private final List<TextDisplay> displayEntities = new ArrayList<>();
    private boolean loaded = false;
    private long lastUpdate = 0;
    
    public Hologram(String id) {
        this.id = id;
        this.type = HologramType.TEXT;
        this.visibilityDistance = -1;
        this.visibility = VisibilityType.ALL;
        this.persistent = true;
        this.scaleX = this.scaleY = this.scaleZ = 1.0f;
        this.translationX = this.translationY = this.translationZ = 0.0f;
        this.shadowRadius = 0.0f;
        this.shadowStrength = 1.0f;
        this.textLines = new ArrayList<>();
        this.textShadow = false;
        this.seeThrough = false;
        this.textAlignment = TextAlignment.CENTER;
        this.updateTextInterval = -1;
        this.background = "transparent";
        this.billboard = BillboardType.VERTICAL;
    }
    
    public void spawn() {
        if (location == null || loaded) return;
        
        despawn();
        
        if (type == HologramType.TEXT && !textLines.isEmpty()) {
            spawnTextDisplay();
        }
        
        loaded = true;
    }
    
    private void spawnTextDisplay() {
        Location spawnLoc = location.clone();
        
        for (int i = 0; i < textLines.size(); i++) {
            TextDisplay display = location.getWorld().spawn(spawnLoc, TextDisplay.class);
            
            // Set text content
            String text = textLines.get(i);
            if (HologramX.getInstance().getPlaceholderManager().isEnabled()) {
                text = HologramX.getInstance().getPlaceholderManager().setPlaceholders(null, text);
            }
            
            Component component = MiniMessage.miniMessage().deserialize(text);
            display.text(component);
            
            // Apply display settings
            applyDisplaySettings(display);
            
            // Set position with line spacing
            double lineSpacing = 0.25;
            Location lineLocation = spawnLoc.clone();
            lineLocation.add(0, -i * lineSpacing, 0);
            display.teleport(lineLocation);
            
            displayEntities.add(display);
        }
    }
    
    private void applyDisplaySettings(TextDisplay display) {
        // Set billboard
        switch (billboard) {
            case FIXED -> display.setBillboard(Display.Billboard.FIXED);
            case VERTICAL -> display.setBillboard(Display.Billboard.VERTICAL);
            case HORIZONTAL -> display.setBillboard(Display.Billboard.HORIZONTAL);
            case CENTER -> display.setBillboard(Display.Billboard.CENTER);
        }
        
        // Set text alignment
        switch (textAlignment) {
            case LEFT -> display.setAlignment(TextDisplay.TextAlignment.LEFT);
            case CENTER -> display.setAlignment(TextDisplay.TextAlignment.CENTER);
            case RIGHT -> display.setAlignment(TextDisplay.TextAlignment.RIGHT);
        }
        
        // Set background
        if (!"transparent".equals(background)) {
            try {
                int color = Integer.parseInt(background.replace("#", ""), 16);
                display.setBackgroundColor(org.bukkit.Color.fromARGB(color));
            } catch (NumberFormatException e) {
                // Use transparent if invalid color
                display.setBackgroundColor(org.bukkit.Color.fromARGB(0));
            }
        } else {
            display.setBackgroundColor(org.bukkit.Color.fromARGB(0));
        }
        
        // Set text properties
        display.setShadowed(textShadow);
        display.setSeeThrough(seeThrough);
        
        // Set transformation
        Vector3f scale = new Vector3f(scaleX, scaleY, scaleZ);
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        Transformation transformation = new Transformation(translation, 
            new org.joml.Quaternionf(), scale, new org.joml.Quaternionf());
        display.setTransformation(transformation);
        
        // Set shadow
        display.setShadowRadius(shadowRadius);
        display.setShadowStrength(shadowStrength);
        
        // Set view range
        if (visibilityDistance > 0) {
            display.setViewRange(visibilityDistance / 16.0f);
        }
    }
    
    public void despawn() {
        displayEntities.forEach(entity -> {
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        });
        displayEntities.clear();
        viewers.clear();
        loaded = false;
    }
    
    public void updateText() {
        if (!loaded || displayEntities.isEmpty()) return;
        
        for (int i = 0; i < Math.min(textLines.size(), displayEntities.size()); i++) {
            TextDisplay display = displayEntities.get(i);
            if (display != null && display.isValid()) {
                String text = textLines.get(i);
                
                // Apply placeholders
                if (HologramX.getInstance().getPlaceholderManager().isEnabled()) {
                    text = HologramX.getInstance().getPlaceholderManager().setPlaceholders(null, text);
                }
                
                Component component = MiniMessage.miniMessage().deserialize(text);
                display.text(component);
            }
        }
        
        lastUpdate = System.currentTimeMillis();
    }
    
    public void updateForPlayer(Player player) {
        if (!canView(player)) {
            if (viewers.contains(player.getUniqueId())) {
                hideFromPlayer(player);
            }
            return;
        }
        
        if (!viewers.contains(player.getUniqueId())) {
            showToPlayer(player);
        }
        
        // Update text with player-specific placeholders
        if (HologramX.getInstance().getPlaceholderManager().isEnabled()) {
            for (int i = 0; i < Math.min(textLines.size(), displayEntities.size()); i++) {
                TextDisplay display = displayEntities.get(i);
                if (display != null && display.isValid()) {
                    String text = textLines.get(i);
                    text = HologramX.getInstance().getPlaceholderManager().setPlaceholders(player, text);
                    Component component = MiniMessage.miniMessage().deserialize(text);
                    display.text(component);
                }
            }
        }
    }
    
    private void showToPlayer(Player player) {
        viewers.add(player.getUniqueId());
        // Display entities are automatically visible to all players
        // This method is for tracking purposes
    }
    
    private void hideFromPlayer(Player player) {
        viewers.remove(player.getUniqueId());
        // We can't hide display entities from specific players
        // This would require per-player spawning which is more complex
    }
    
    public boolean canView(Player player) {
        // Check visibility distance
        if (visibilityDistance > 0 && location != null) {
            double distance = player.getLocation().distance(location);
            if (distance > visibilityDistance) {
                return false;
            }
        }
        
        // Check visibility type
        switch (visibility) {
            case ALL -> {
                return true;
            }
            case PERMISSION -> {
                return player.hasPermission("hologramx.view." + id);
            }
            case WORLD -> {
                return player.getWorld().equals(location.getWorld());
            }
            case NONE -> {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean needsUpdate() {
        return updateTextInterval > 0 && 
               System.currentTimeMillis() - lastUpdate >= updateTextInterval * 50L;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public HologramType getType() { return type; }
    public void setType(HologramType type) { this.type = type; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public int getVisibilityDistance() { return visibilityDistance; }
    public void setVisibilityDistance(int visibilityDistance) { this.visibilityDistance = visibilityDistance; }
    public VisibilityType getVisibility() { return visibility; }
    public void setVisibility(VisibilityType visibility) { this.visibility = visibility; }
    public boolean isPersistent() { return persistent; }
    public void setPersistent(boolean persistent) { this.persistent = persistent; }
    public float getScaleX() { return scaleX; }
    public void setScaleX(float scaleX) { this.scaleX = scaleX; }
    public float getScaleY() { return scaleY; }
    public void setScaleY(float scaleY) { this.scaleY = scaleY; }
    public float getScaleZ() { return scaleZ; }
    public void setScaleZ(float scaleZ) { this.scaleZ = scaleZ; }
    public float getTranslationX() { return translationX; }
    public void setTranslationX(float translationX) { this.translationX = translationX; }
    public float getTranslationY() { return translationY; }
    public void setTranslationY(float translationY) { this.translationY = translationY; }
    public float getTranslationZ() { return translationZ; }
    public void setTranslationZ(float translationZ) { this.translationZ = translationZ; }
    public float getShadowRadius() { return shadowRadius; }
    public void setShadowRadius(float shadowRadius) { this.shadowRadius = shadowRadius; }
    public float getShadowStrength() { return shadowStrength; }
    public void setShadowStrength(float shadowStrength) { this.shadowStrength = shadowStrength; }
    public List<String> getTextLines() { return textLines; }
    public void setTextLines(List<String> textLines) { this.textLines = textLines; }
    public boolean isTextShadow() { return textShadow; }
    public void setTextShadow(boolean textShadow) { this.textShadow = textShadow; }
    public boolean isSeeThrough() { return seeThrough; }
    public void setSeeThrough(boolean seeThrough) { this.seeThrough = seeThrough; }
    public TextAlignment getTextAlignment() { return textAlignment; }
    public void setTextAlignment(TextAlignment textAlignment) { this.textAlignment = textAlignment; }
    public int getUpdateTextInterval() { return updateTextInterval; }
    public void setUpdateTextInterval(int updateTextInterval) { this.updateTextInterval = updateTextInterval; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public BillboardType getBillboard() { return billboard; }
    public void setBillboard(BillboardType billboard) { this.billboard = billboard; }
    public boolean isLoaded() { return loaded; }
    public Set<UUID> getViewers() { return viewers; }
    public List<TextDisplay> getDisplayEntities() { return displayEntities; }
    
    // Enums
    public enum HologramType {
        TEXT, ITEM, BLOCK
    }
    
    public enum VisibilityType {
        ALL, PERMISSION, WORLD, NONE
    }
    
    public enum TextAlignment {
        LEFT, CENTER, RIGHT
    }
    
    public enum BillboardType {
        FIXED, VERTICAL, HORIZONTAL, CENTER
    }
}