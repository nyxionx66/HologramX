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
    
    private void handleMove(CommandSender sender, String[] args) {
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
                "usage", "/hx move <name>");
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
    
    private void handleText(CommandSender sender, String[] args) {
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
                "usage", "/hx text <name> <add|remove|set|clear> [args...]");
            return;
        }
        
        String name = args[1];
        String action = args[2].toLowerCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            plugin.getMessages().sendMessage(player, "error-invalid-location");
            return;
        }
        
        switch (action) {
            case "add" -> {
                if (args.length < 4) {
                    plugin.getMessages().sendMessage(player, "invalid-syntax", 
                        "usage", "/hx text <name> add <text>");
                    return;
                }
                
                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                hologram.getTextLines().add(text);
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-line-added", "name", name);
            }
            case "remove" -> {
                if (args.length < 4) {
                    plugin.getMessages().sendMessage(player, "invalid-syntax", 
                        "usage", "/hx text <name> remove <line>");
                    return;
                }
                
                try {
                    int line = Integer.parseInt(args[3]) - 1;
                    if (line >= 0 && line < hologram.getTextLines().size()) {
                        hologram.getTextLines().remove(line);
                        hologram.despawn();
                        hologram.spawn();
                        
                        plugin.getMessages().sendMessage(player, "text-line-removed", 
                            "name", name, "line", String.valueOf(line + 1));
                    } else {
                        plugin.getMessages().sendMessage(player, "text-line-invalid", 
                            "max", String.valueOf(hologram.getTextLines().size()));
                    }
                } catch (NumberFormatException e) {
                    plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[3]);
                }
            }
            case "clear" -> {
                hologram.getTextLines().clear();
                hologram.despawn();
                hologram.spawn();
                
                plugin.getMessages().sendMessage(player, "text-cleared", "name", name);
            }
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
        sender.sendMessage("§e/hx create <name> <type> §7- Create hologram");
        sender.sendMessage("§e/hx delete <name> §7- Delete hologram");
        sender.sendMessage("§e/hx list [page] §7- List holograms");
        sender.sendMessage("§e/hx info <name> §7- Show hologram info");
        sender.sendMessage("§e/hx move <name> §7- Move hologram");
        sender.sendMessage("§e/hx text <name> <add|remove|clear> §7- Edit text");
        sender.sendMessage("§e/hx reload §7- Reload plugin");
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