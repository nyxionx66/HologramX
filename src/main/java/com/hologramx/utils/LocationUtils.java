package com.hologramx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {
    
    public static String locationToString(Location location) {
        if (location == null) return null;
        
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }
    
    public static Location stringToLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        
        String[] parts = locationString.split(",");
        if (parts.length != 6) return null;
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static String formatLocation(Location location) {
        if (location == null) return "Unknown";
        
        return String.format("%s (%.1f, %.1f, %.1f)",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }
    
    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return -1;
        if (!loc1.getWorld().equals(loc2.getWorld())) return -1;
        
        return loc1.distance(loc2);
    }
}