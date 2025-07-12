package com.hologramx.commands;

import com.hologramx.HologramX;
import com.hologramx.holograms.Hologram;
import com.hologramx.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HologramCommand implements CommandExecutor, TabCompleter {
    
    private final HologramX plugin;
    
    public HologramCommand(HologramX plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            // Basic commands
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender, args);
            case "info" -> handleInfo(sender, args);
            case "toggle" -> handleToggle(sender, args);
            case "clone" -> handleClone(sender, args);
            case "near" -> handleNear(sender, args);
            case "tp", "teleport" -> handleTeleport(sender, args);
            case "reload" -> handleReload(sender, args);
            
            // General hologram properties
            case "movehere", "position" -> handleMoveHere(sender, args);
            case "moveto" -> handleMoveTo(sender, args);
            case "rotate" -> handleRotate(sender, args);
            case "rotatepitch" -> handleRotatePitch(sender, args);
            case "visibilitydistance" -> handleVisibilityDistance(sender, args);
            case "visibility" -> handleVisibility(sender, args);
            case "scale" -> handleScale(sender, args);
            case "billboard" -> handleBillboard(sender, args);
            case "shadowstrength" -> handleShadowStrength(sender, args);
            case "shadowradius" -> handleShadowRadius(sender, args);
            
            // Text hologram commands
            case "setline" -> handleSetLine(sender, args);
            case "addline" -> handleAddLine(sender, args);
            case "removeline" -> handleRemoveLine(sender, args);
            case "insertbefore" -> handleInsertBefore(sender, args);
            case "insertafter" -> handleInsertAfter(sender, args);
            case "updatetextinterval" -> handleUpdateTextInterval(sender, args);
            case "background" -> handleBackground(sender, args);
            case "textshadow" -> handleTextShadow(sender, args);
            case "textalignment" -> handleTextAlignment(sender, args);
            
            default -> sendUsage(sender);
        }
        
        return true;
    }
    
    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.create")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx create <name> <type>");
            return;
        }
        
        String name = args[1];
        String typeStr = args[2].toUpperCase();
        
        if (plugin.getHologramManager().getHologram(name) != null) {
            plugin.getMessages().sendMessage(player, "hologram-already-exists", "name", name);
            return;
        }
        
        Hologram.HologramType type;
        try {
            type = Hologram.HologramType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-location");
            return;
        }
        
        Location location = player.getLocation();
        Hologram hologram = plugin.getHologramManager().createHologram(name, type, location);
        
        if (hologram != null) {
            plugin.getMessages().sendMessage(player, "hologram-created", "name", name);
        } else {
            plugin.getMessages().sendMessage(player, "hologram-already-exists", "name", name);
        }
    }
    
    private void handleDelete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.delete")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx delete <name>");
            return;
        }
        
        String name = args[1];
        
        if (plugin.getHologramManager().deleteHologram(name)) {
            plugin.getMessages().sendMessage(player, "hologram-deleted", "name", name);
        } else {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
        }
    }
    
    private void handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.use")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        Collection<Hologram> holograms = plugin.getHologramManager().getHolograms();
        
        if (holograms.isEmpty()) {
            plugin.getMessages().sendMessage(player, "hologram-list-empty");
            return;
        }
        
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int perPage = 10;
        int maxPages = (int) Math.ceil((double) holograms.size() / perPage);
        page = Math.max(1, Math.min(page, maxPages));
        
        plugin.getMessages().sendMessage(player, "hologram-list-header", 
            "page", String.valueOf(page), "max_pages", String.valueOf(maxPages));
        
        List<Hologram> hologramList = new ArrayList<>(holograms);
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, hologramList.size());
        
        for (int i = start; i < end; i++) {
            Hologram hologram = hologramList.get(i);
            Location loc = hologram.getLocation();
            
            plugin.getMessages().sendMessage(player, "hologram-list-entry",
                "name", hologram.getId(),
                "type", hologram.getType().name(),
                "world", loc != null ? loc.getWorld().getName() : "Unknown",
                "x", loc != null ? String.format("%.1f", loc.getX()) : "?",
                "y", loc != null ? String.format("%.1f", loc.getY()) : "?",
                "z", loc != null ? String.format("%.1f", loc.getZ()) : "?");
        }
    }
    
    private void handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.use")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx info <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        Location loc = hologram.getLocation();
        
        plugin.getMessages().sendMessage(player, "hologram-info-header", "name", name);
        plugin.getMessages().sendMessage(player, "hologram-info-type", "type", hologram.getType().name());
        
        if (loc != null) {
            plugin.getMessages().sendMessage(player, "hologram-info-location",
                "world", loc.getWorld().getName(),
                "x", String.format("%.2f", loc.getX()),
                "y", String.format("%.2f", loc.getY()),
                "z", String.format("%.2f", loc.getZ()));
        }
        
        plugin.getMessages().sendMessage(player, "hologram-info-visibility", 
            "visibility", hologram.getVisibility().name());
        plugin.getMessages().sendMessage(player, "hologram-info-distance", 
            "distance", String.valueOf(hologram.getVisibilityDistance()));
        plugin.getMessages().sendMessage(player, "hologram-info-scale",
            "scale_x", String.valueOf(hologram.getScaleX()),
            "scale_y", String.valueOf(hologram.getScaleY()),
            "scale_z", String.valueOf(hologram.getScaleZ()));
        plugin.getMessages().sendMessage(player, "hologram-info-persistent", 
            "persistent", String.valueOf(hologram.isPersistent()));
    }
    
    // General Hologram Property Commands
    
    private void handleMoveHere(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.move")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx moveHere <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        hologram.despawn();
        hologram.setLocation(player.getLocation());
        hologram.spawn();
        
        plugin.getMessages().sendMessage(player, "hologram-moved", "name", name);
    }
    
    private void handleMoveTo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.move")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 5) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx moveTo <name> <x> <y> <z> [yaw] [pitch]");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);
            float yaw = args.length > 5 ? Float.parseFloat(args[5]) : 0.0f;
            float pitch = args.length > 6 ? Float.parseFloat(args[6]) : 0.0f;
            
            Location newLocation = new Location(hologram.getLocation().getWorld(), x, y, z, yaw, pitch);
            
            hologram.despawn();
            hologram.setLocation(newLocation);
            hologram.spawn();
            
            plugin.getMessages().sendMessage(player, "hologram-moved", "name", name);
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", "coordinates");
        }
    }
    
    private void handleRotate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx rotate <name> <degrees>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            float degrees = Float.parseFloat(args[2]);
            Location loc = hologram.getLocation();
            loc.setYaw(degrees);
            
            hologram.despawn();
            hologram.setLocation(loc);
            hologram.spawn();
            
            player.sendMessage("§aRotated hologram '" + name + "' to " + degrees + " degrees (Y-axis).");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleRotatePitch(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx rotatePitch <name> <degrees>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            float degrees = Float.parseFloat(args[2]);
            Location loc = hologram.getLocation();
            loc.setPitch(degrees);
            
            hologram.despawn();
            hologram.setLocation(loc);
            hologram.spawn();
            
            player.sendMessage("§aRotated hologram '" + name + "' pitch to " + degrees + " degrees (X-axis).");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleVisibilityDistance(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx visibilityDistance <name> <distance>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            int distance = Integer.parseInt(args[2]);
            hologram.setVisibilityDistance(distance);
            
            hologram.despawn();
            hologram.spawn();
            
            String distanceText = distance == -1 ? "unlimited" : distance + " blocks";
            player.sendMessage("§aSet visibility distance for hologram '" + name + "' to " + distanceText + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleVisibility(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx visibility <name> <ALL|MANUAL|PERMISSION_NEEDED>");
            return;
        }
        
        String name = args[1];
        String visibilityType = args[2].toUpperCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            Hologram.VisibilityType visibility;
            switch (visibilityType) {
                case "ALL" -> visibility = Hologram.VisibilityType.ALL;
                case "MANUAL" -> visibility = Hologram.VisibilityType.NONE;
                case "PERMISSION_NEEDED" -> visibility = Hologram.VisibilityType.PERMISSION;
                default -> {
                    player.sendMessage("§cInvalid visibility type! Use: ALL, MANUAL, or PERMISSION_NEEDED");
                    return;
                }
            }
            
            hologram.setVisibility(visibility);
            player.sendMessage("§aSet visibility for hologram '" + name + "' to " + visibilityType + ".");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid visibility type! Use: ALL, MANUAL, or PERMISSION_NEEDED");
        }
    }
    
    private void handleScale(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx scale <name> <factor>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            float factor = Float.parseFloat(args[2]);
            hologram.setScaleX(factor);
            hologram.setScaleY(factor);
            hologram.setScaleZ(factor);
            
            hologram.despawn();
            hologram.spawn();
            
            player.sendMessage("§aScaled hologram '" + name + "' by factor " + factor + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleBillboard(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx billboard <name> <center|fixed|vertical|horizontal>");
            return;
        }
        
        String name = args[1];
        String billboardType = args[2].toUpperCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            Hologram.BillboardType billboard = Hologram.BillboardType.valueOf(billboardType);
            hologram.setBillboard(billboard);
            
            hologram.despawn();
            hologram.spawn();
            
            player.sendMessage("§aSet billboard mode for hologram '" + name + "' to " + billboardType + ".");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid billboard type! Use: center, fixed, vertical, or horizontal");
        }
    }
    
    private void handleShadowStrength(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx shadowStrength <name> <value>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            float strength = Float.parseFloat(args[2]);
            hologram.setShadowStrength(strength);
            
            hologram.despawn();
            hologram.spawn();
            
            player.sendMessage("§aSet shadow strength for hologram '" + name + "' to " + strength + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleShadowRadius(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx shadowRadius <name> <radius>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            float radius = Float.parseFloat(args[2]);
            hologram.setShadowRadius(radius);
            
            hologram.despawn();
            hologram.spawn();
            
            player.sendMessage("§aSet shadow radius for hologram '" + name + "' to " + radius + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx toggle <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.isLoaded()) {
            hologram.despawn();
        } else {
            hologram.spawn();
        }
        
        plugin.getMessages().sendMessage(player, "hologram-toggled", "name", name);
    }
    
    private void handleClone(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.create")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx clone <name> <new_name>");
            return;
        }
        
        String name = args[1];
        String newName = args[2];
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (plugin.getHologramManager().getHologram(newName) != null) {
            plugin.getMessages().sendMessage(player, "hologram-already-exists", "name", newName);
            return;
        }
        
        Hologram newHologram = plugin.getHologramManager().createHologram(
            newName, hologram.getType(), player.getLocation());
        
        if (newHologram != null) {
            // Copy properties
            newHologram.setTextLines(new ArrayList<>(hologram.getTextLines()));
            newHologram.setScaleX(hologram.getScaleX());
            newHologram.setScaleY(hologram.getScaleY());
            newHologram.setScaleZ(hologram.getScaleZ());
            newHologram.setVisibility(hologram.getVisibility());
            newHologram.setVisibilityDistance(hologram.getVisibilityDistance());
            
            newHologram.despawn();
            newHologram.spawn();
            
            plugin.getMessages().sendMessage(player, "hologram-cloned", 
                "name", name, "new_name", newName);
        }
    }
    
    private void handleNear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.use")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        double radius = 50.0;
        if (args.length > 1) {
            try {
                radius = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                radius = 50.0;
            }
        }
        
        List<Hologram> nearby = plugin.getHologramManager().getNearbyHolograms(
            player.getLocation(), radius);
        
        if (nearby.isEmpty()) {
            plugin.getMessages().sendMessage(player, "near-empty");
            return;
        }
        
        plugin.getMessages().sendMessage(player, "near-header", 
            "distance", String.valueOf((int) radius));
        
        for (Hologram hologram : nearby) {
            double distance = player.getLocation().distance(hologram.getLocation());
            plugin.getMessages().sendMessage(player, "near-entry",
                "name", hologram.getId(),
                "distance", String.format("%.1f", distance));
        }
    }
    
    private void handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.use")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx tp <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        Location location = hologram.getLocation();
        if (location != null) {
            player.teleport(location);
            plugin.getMessages().sendMessage(player, "hologram-teleported", "name", name);
        }
    }
    
    // Text Hologram Commands
    
    private void handleSetLine(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 4) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx setLine <name> <line> <text...>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            int lineNumber = Integer.parseInt(args[2]) - 1;
            String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            
            if (lineNumber >= 0 && lineNumber < hologram.getTextLines().size()) {
                hologram.getTextLines().set(lineNumber, text);
                
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-line-set", 
                    "name", name, "line", String.valueOf(lineNumber + 1));
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleAddLine(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx addLine <name> <text...>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        hologram.getTextLines().add(text);
        
        hologram.despawn();
        hologram.spawn();
        
        plugin.getMessages().sendMessage(player, "text-line-added", "name", name);
    }
    
    private void handleRemoveLine(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx removeLine <name> <line>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            int lineNumber = Integer.parseInt(args[2]) - 1;
            
            if (lineNumber >= 0 && lineNumber < hologram.getTextLines().size()) {
                hologram.getTextLines().remove(lineNumber);
                
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-line-removed", 
                    "name", name, "line", String.valueOf(lineNumber + 1));
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleInsertBefore(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 4) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx insertBefore <name> <line> <text...>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            int lineNumber = Integer.parseInt(args[2]) - 1;
            String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            
            if (lineNumber >= 0 && lineNumber <= hologram.getTextLines().size()) {
                hologram.getTextLines().add(lineNumber, text);
                
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-line-inserted", 
                    "name", name, "line", String.valueOf(lineNumber + 1));
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleInsertAfter(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 4) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx insertAfter <name> <line> <text...>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            int lineNumber = Integer.parseInt(args[2]);
            String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            
            if (lineNumber >= 1 && lineNumber <= hologram.getTextLines().size()) {
                hologram.getTextLines().add(lineNumber, text);
                
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-line-inserted", 
                    "name", name, "line", String.valueOf(lineNumber + 1));
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
        }
    }
    
    private void handleUpdateTextInterval(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx updateTextInterval <name> <ticks|seconds|minutes>");
            return;
        }
        
        String name = args[1];
        String intervalStr = args[2].toLowerCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            int interval;
            if (intervalStr.endsWith("t") || intervalStr.endsWith("ticks")) {
                interval = Integer.parseInt(intervalStr.replaceAll("[^0-9]", ""));
            } else if (intervalStr.endsWith("s") || intervalStr.endsWith("seconds")) {
                interval = Integer.parseInt(intervalStr.replaceAll("[^0-9]", "")) * 20;
            } else if (intervalStr.endsWith("m") || intervalStr.endsWith("minutes")) {
                interval = Integer.parseInt(intervalStr.replaceAll("[^0-9]", "")) * 1200;
            } else {
                interval = Integer.parseInt(intervalStr);
            }
            
            hologram.setUpdateTextInterval(interval);
            
            String intervalText = interval == -1 ? "disabled" : interval + " ticks";
            player.sendMessage("§aSet update interval for hologram '" + name + "' to " + intervalText + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", intervalStr);
        }
    }
    
    private void handleBackground(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx background <name> <color>");
            return;
        }
        
        String name = args[1];
        String color = args[2];
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        hologram.setBackground(color);
        
        hologram.despawn();
        hologram.spawn();
        
        player.sendMessage("§aSet background color for hologram '" + name + "' to " + color + ".");
    }
    
    private void handleTextShadow(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx textShadow <name> <true|false>");
            return;
        }
        
        String name = args[1];
        String shadowStr = args[2].toLowerCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        boolean shadow = "true".equals(shadowStr) || "on".equals(shadowStr) || "yes".equals(shadowStr);
        hologram.setTextShadow(shadow);
        
        hologram.despawn();
        hologram.spawn();
        
        player.sendMessage("§aSet text shadow for hologram '" + name + "' to " + shadow + ".");
    }
    
    private void handleTextAlignment(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/hx textAlignment <name> <center|left|right>");
            return;
        }
        
        String name = args[1];
        String alignmentStr = args[2].toUpperCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        try {
            Hologram.TextAlignment alignment = Hologram.TextAlignment.valueOf(alignmentStr);
            hologram.setTextAlignment(alignment);
            
            hologram.despawn();
            hologram.spawn();
            
            player.sendMessage("§aSet text alignment for hologram '" + name + "' to " + alignmentStr + ".");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid alignment! Use: center, left, or right");
        }
    }
    
    private void handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hologramx.reload")) {
            if (sender instanceof Player player) {
                plugin.getMessages().sendMessage(player, "no-permission");
            }
            return;
        }
        
        plugin.reload();
        
        if (sender instanceof Player player) {
            plugin.getMessages().sendMessage(player, "plugin-reloaded");
        } else {
            sender.sendMessage("HologramX has been reloaded!");
        }
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6HologramX Commands:");
        sender.sendMessage("§e§lBasic Commands:");
        sender.sendMessage("§e/hx create <name> <type> §7- Create hologram");
        sender.sendMessage("§e/hx delete <name> §7- Delete hologram");
        sender.sendMessage("§e/hx list [page] §7- List holograms");
        sender.sendMessage("§e/hx info <name> §7- Show hologram info");
        sender.sendMessage("§e/hx clone <name> <new_name> §7- Clone hologram");
        sender.sendMessage("§e/hx reload §7- Reload plugin");
        
        sender.sendMessage("§e§lGeneral Properties:");
        sender.sendMessage("§e/hx moveHere <name> §7- Move to your location");
        sender.sendMessage("§e/hx moveTo <name> <x> <y> <z> [yaw] [pitch] §7- Set exact position");
        sender.sendMessage("§e/hx rotate <name> <degrees> §7- Rotate Y-axis");
        sender.sendMessage("§e/hx rotatePitch <name> <degrees> §7- Rotate X-axis");
        sender.sendMessage("§e/hx visibilityDistance <name> <distance> §7- Set view distance");
        sender.sendMessage("§e/hx visibility <name> <ALL|MANUAL|PERMISSION_NEEDED> §7- Set visibility");
        sender.sendMessage("§e/hx scale <name> <factor> §7- Resize hologram");
        sender.sendMessage("§e/hx billboard <name> <center|fixed|vertical|horizontal> §7- Set orientation");
        sender.sendMessage("§e/hx shadowStrength <name> <value> §7- Shadow quality");
        sender.sendMessage("§e/hx shadowRadius <name> <radius> §7- Shadow spread");
        
        sender.sendMessage("§e§lText Commands:");
        sender.sendMessage("§e/hx setLine <name> <line> <text...> §7- Replace line");
        sender.sendMessage("§e/hx addLine <name> <text...> §7- Add new line");
        sender.sendMessage("§e/hx removeLine <name> <line> §7- Remove line");
        sender.sendMessage("§e/hx insertBefore <name> <line> <text...> §7- Insert above");
        sender.sendMessage("§e/hx insertAfter <name> <line> <text...> §7- Insert below");
        sender.sendMessage("§e/hx updateTextInterval <name> <time> §7- Auto-refresh rate");
        sender.sendMessage("§e/hx background <name> <color> §7- Background color");
        sender.sendMessage("§e/hx textShadow <name> <true|false> §7- Text shadow");
        sender.sendMessage("§e/hx textAlignment <name> <center|left|right> §7- Text alignment");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("create", "delete", "list", "info", 
                "move", "toggle", "clone", "near", "tp", "text", "reload");
            return commands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (Arrays.asList("delete", "info", "move", "toggle", "clone", "tp", "text").contains(subCommand)) {
                return plugin.getHologramManager().getHolograms().stream()
                    .map(Hologram::getId)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("create".equals(subCommand)) {
                return Arrays.asList("TEXT", "ITEM", "BLOCK").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            if ("create".equals(args[0].toLowerCase())) {
                return Arrays.asList("TEXT", "ITEM", "BLOCK").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("text".equals(args[0].toLowerCase())) {
                return Arrays.asList("add", "remove", "set", "clear").stream()
                    .filter(action -> action.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}