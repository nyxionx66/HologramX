package com.hologramx.commands;

import com.hologramx.HologramX;
import com.hologramx.holograms.Hologram;
import com.hologramx.utils.LocationUtils;
import com.hologramx.utils.ColorUtils;
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
            
            // Edit command (unified editing) - ONLY WAY TO EDIT
            case "edit" -> handleEdit(sender, args);
            
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
    
    // Unified Edit Command
    
    private void handleEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().sendMessage((Player) sender, "player-only");
            return;
        }
        
        if (!sender.hasPermission("hologramx.edit")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 3) {
            sendEditUsage(player);
            return;
        }
        
        String hologramName = args[1];
        String editCommand = args[2].toLowerCase();
        
        Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", hologramName);
            return;
        }
        
        // Create new args array for the edit subcommand
        String[] editArgs = new String[args.length - 1];
        editArgs[0] = editCommand;
        editArgs[1] = hologramName;
        System.arraycopy(args, 3, editArgs, 2, args.length - 3);
        
        switch (editCommand) {
            // General properties
            case "movehere", "position" -> handleMoveHere(sender, editArgs);
            case "moveto" -> handleMoveTo(sender, editArgs);
            case "rotate" -> handleRotate(sender, editArgs);
            case "rotatepitch" -> handleRotatePitch(sender, editArgs);
            case "visibilitydistance" -> handleVisibilityDistance(sender, editArgs);
            case "visibility" -> handleVisibility(sender, editArgs);
            case "scale" -> handleScale(sender, editArgs);
            case "billboard" -> handleBillboard(sender, editArgs);
            case "shadowstrength" -> handleShadowStrength(sender, editArgs);
            case "shadowradius" -> handleShadowRadius(sender, editArgs);
            
            // Text commands
            case "setline" -> handleSetLine(sender, editArgs);
            case "addline" -> handleAddLine(sender, editArgs);
            case "removeline" -> handleRemoveLine(sender, editArgs);
            case "insertbefore" -> handleInsertBefore(sender, editArgs);
            case "insertafter" -> handleInsertAfter(sender, editArgs);
            case "updatetextinterval" -> handleUpdateTextInterval(sender, editArgs);
            case "background" -> handleBackground(sender, editArgs);
            case "textshadow" -> handleTextShadow(sender, editArgs);
            case "textalignment" -> handleTextAlignment(sender, editArgs);
            case "cleartext" -> {
                if (hologram.getType() == Hologram.HologramType.TEXT) {
                    hologram.clearTextLines();
                    hologram.refresh();
                    player.sendMessage("§aCleared all text lines for hologram '" + hologramName + "'.");
                } else {
                    player.sendMessage("§cThis command only works with text holograms!");
                }
            }
            
            // Line-specific scaling commands
            case "linescale" -> handleLineScale(sender, editArgs);
            case "linescalex" -> handleLineScaleX(sender, editArgs);
            case "linescaley" -> handleLineScaleY(sender, editArgs);
            case "linescalez" -> handleLineScaleZ(sender, editArgs);
            case "linespacing" -> handleLineSpacing(sender, editArgs);
            
            default -> sendEditUsage(player);
        }
    }
    
    private void sendEditUsage(Player player) {
        player.sendMessage("§6Edit Hologram Commands:");
        player.sendMessage("§e§lGeneral Properties:");
        player.sendMessage("§e/hx edit <name> moveHere §7- Move to your location");
        player.sendMessage("§e/hx edit <name> moveTo <x> <y> <z> [yaw] [pitch] §7- Set position");
        player.sendMessage("§e/hx edit <name> rotate <degrees> §7- Rotate Y-axis");
        player.sendMessage("§e/hx edit <name> rotatePitch <degrees> §7- Rotate X-axis");
        player.sendMessage("§e/hx edit <name> visibilityDistance <distance> §7- Set view distance");
        player.sendMessage("§e/hx edit <name> visibility <type> §7- Set visibility");
        player.sendMessage("§e/hx edit <name> scale <factor> §7- Resize");
        player.sendMessage("§e/hx edit <name> billboard <type> §7- Set orientation");
        player.sendMessage("§e/hx edit <name> shadowStrength <value> §7- Shadow quality");
        player.sendMessage("§e/hx edit <name> shadowRadius <radius> §7- Shadow spread");
        
        player.sendMessage("§e§lText Properties:");
        player.sendMessage("§e/hx edit <name> setLine <line> <text...> §7- Replace line");
        player.sendMessage("§e/hx edit <name> addLine <text...> §7- Add line");
        player.sendMessage("§e/hx edit <name> removeLine <line> §7- Remove line");
        player.sendMessage("§e/hx edit <name> insertBefore <line> <text...> §7- Insert above");
        player.sendMessage("§e/hx edit <name> insertAfter <line> <text...> §7- Insert below");
        player.sendMessage("§e/hx edit <name> clearText §7- Clear all text");
        player.sendMessage("§e/hx edit <name> background <color> §7- Background color");
        player.sendMessage("§e/hx edit <name> textShadow <true|false> §7- Text shadow");
        player.sendMessage("§e/hx edit <name> textAlignment <type> §7- Text alignment");
        player.sendMessage("§e/hx edit <name> updateTextInterval <time> §7- Auto-refresh");
        
        player.sendMessage("§e§lLine-Specific Scaling:");
        player.sendMessage("§e/hx edit <name> lineScale <line> <factor> §7- Scale specific line");
        player.sendMessage("§e/hx edit <name> lineScaleX <line> <factor> §7- Scale line X-axis");
        player.sendMessage("§e/hx edit <name> lineScaleY <line> <factor> §7- Scale line Y-axis");
        player.sendMessage("§e/hx edit <name> lineScaleZ <line> <factor> §7- Scale line Z-axis");
        player.sendMessage("§e/hx edit <name> lineSpacing <value> §7- Set line spacing");
    }
    
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
            
            hologram.refresh();
            
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
        hologram.addTextLine(text);
        
        hologram.refresh();
        
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
                hologram.removeTextLine(lineNumber);
                
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
                hologram.insertTextLine(lineNumber, text);
                
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
                hologram.insertTextLine(lineNumber, text);
                
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
        String colorInput = args[2];
        
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (hologram.getType() != Hologram.HologramType.TEXT) {
            player.sendMessage("§cThis command only works with text holograms!");
            return;
        }
        
        String parsedColor = ColorUtils.parseColor(colorInput);
        if (parsedColor == null) {
            player.sendMessage("§cInvalid color format! Use: transparent, color names, #RRGGBB, or rgb(r,g,b)");
            player.sendMessage("§7Examples: transparent, red, #FF0000, rgb(255,0,0)");
            return;
        }
        
        hologram.setBackground(parsedColor);
        
        hologram.despawn();
        hologram.spawn();
        
        String displayColor = ColorUtils.formatColorForDisplay(parsedColor);
        player.sendMessage("§aSet background color for hologram '" + name + "' to " + displayColor + ".");
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
    
    // Line-specific scaling commands
    
    private void handleLineScale(CommandSender sender, String[] args) {
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
                "usage", "/hx lineScale <name> <line> <factor>");
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
            float scale = Float.parseFloat(args[3]);
            
            if (lineNumber >= 0 && lineNumber < hologram.getTextLines().size()) {
                hologram.setLineScaleUniform(lineNumber, scale);
                
                hologram.refresh();
                
                player.sendMessage("§aSet scale for line " + (lineNumber + 1) + " of hologram '" + name + "' to " + scale + ".");
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", "line or scale");
        }
    }
    
    private void handleLineScaleX(CommandSender sender, String[] args) {
        handleLineScaleAxis(sender, args, "X");
    }
    
    private void handleLineScaleY(CommandSender sender, String[] args) {
        handleLineScaleAxis(sender, args, "Y");
    }
    
    private void handleLineScaleZ(CommandSender sender, String[] args) {
        handleLineScaleAxis(sender, args, "Z");
    }
    
    private void handleLineScaleAxis(CommandSender sender, String[] args, String axis) {
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
                "usage", "/hx lineScale" + axis + " <name> <line> <factor>");
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
            float scale = Float.parseFloat(args[3]);
            
            if (lineNumber >= 0 && lineNumber < hologram.getTextLines().size()) {
                float currentX = hologram.getLineScaleX(lineNumber);
                float currentY = hologram.getLineScaleY(lineNumber);
                float currentZ = hologram.getLineScaleZ(lineNumber);
                
                switch (axis) {
                    case "X" -> hologram.setLineScale(lineNumber, scale, currentY, currentZ);
                    case "Y" -> hologram.setLineScale(lineNumber, currentX, scale, currentZ);
                    case "Z" -> hologram.setLineScale(lineNumber, currentX, currentY, scale);
                }
                
                hologram.refresh();
                
                player.sendMessage("§aSet " + axis + "-scale for line " + (lineNumber + 1) + " of hologram '" + name + "' to " + scale + ".");
            } else {
                plugin.getMessages().sendMessage(player, "text-line-invalid", 
                    "max", String.valueOf(hologram.getTextLines().size()));
            }
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", "line or scale");
        }
    }
    
    private void handleLineSpacing(CommandSender sender, String[] args) {
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
                "usage", "/hx edit <name> lineSpacing <value>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        try {
            double spacing = Double.parseDouble(args[2]);
            hologram.setLineSpacing(spacing);
            
            hologram.refresh();
            
            player.sendMessage("§aSet line spacing for hologram '" + name + "' to " + spacing + ".");
            
        } catch (NumberFormatException e) {
            plugin.getMessages().sendMessage(player, "error-invalid-number", "value", args[2]);
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
        
        sender.sendMessage("§e§lEdit Command:");
        sender.sendMessage("§e/hx edit <name> <property> [args...] §7- Edit hologram properties");
        sender.sendMessage("§7Use '/hx edit' without arguments for detailed edit commands");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList(
                "create", "delete", "list", "info", "toggle", "clone", "near", "tp", "reload", "edit",
                "moveHere", "position", "moveTo", "rotate", "rotatePitch", "visibilityDistance", 
                "visibility", "scale", "billboard", "shadowStrength", "shadowRadius",
                "setLine", "addLine", "removeLine", "insertBefore", "insertAfter", 
                "updateTextInterval", "background", "textShadow", "textAlignment",
                "lineScale", "lineScaleX", "lineScaleY", "lineScaleZ"
            );
            return commands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            // Commands that need hologram names
            if (Arrays.asList("delete", "info", "toggle", "clone", "tp", "edit", "movehere", "position", 
                "moveto", "rotate", "rotatepitch", "visibilitydistance", "visibility", "scale", 
                "billboard", "shadowstrength", "shadowradius", "setline", "addline", "removeline", 
                "insertbefore", "insertafter", "updatetextinterval", "background", "textshadow", 
                "textalignment", "linescale", "linescalex", "linescaley", "linescalez").contains(subCommand)) {
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
            String subCommand = args[0].toLowerCase();
            
            if ("create".equals(subCommand)) {
                return Arrays.asList("TEXT", "ITEM", "BLOCK").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            // Edit command completions
            if ("edit".equals(subCommand)) {
                List<String> editCommands = Arrays.asList(
                    "moveHere", "moveTo", "rotate", "rotatePitch", "visibilityDistance", "visibility",
                    "scale", "billboard", "shadowStrength", "shadowRadius", "setLine", "addLine",
                    "removeLine", "insertBefore", "insertAfter", "updateTextInterval", "background",
                    "textShadow", "textAlignment", "clearText", "lineScale", "lineScaleX", "lineScaleY", "lineScaleZ"
                );
                return editCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("visibility".equals(subCommand)) {
                return Arrays.asList("ALL", "MANUAL", "PERMISSION_NEEDED").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("billboard".equals(subCommand)) {
                return Arrays.asList("center", "fixed", "vertical", "horizontal").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("textshadow".equals(subCommand)) {
                return Arrays.asList("true", "false").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("textalignment".equals(subCommand)) {
                return Arrays.asList("center", "left", "right").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if ("background".equals(subCommand)) {
                return ColorUtils.getColorSuggestions(args[2]);
            }
            
            // Line number completions for line-specific commands
            if (Arrays.asList("setline", "removeline", "insertbefore", "insertafter", 
                "linescale", "linescalex", "linescaley", "linescalez").contains(subCommand)) {
                Hologram hologram = plugin.getHologramManager().getHologram(args[1]);
                if (hologram != null && hologram.getType() == Hologram.HologramType.TEXT) {
                    return getLineNumberCompletions(hologram, subCommand, args[2]);
                }
            }
        }
        
        if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            
            // For direct line commands
            if (Arrays.asList("setline", "removeline", "insertbefore", "insertafter", 
                "linescale", "linescalex", "linescaley", "linescalez").contains(subCommand)) {
                
                if (Arrays.asList("linescale", "linescalex", "linescaley", "linescalez").contains(subCommand)) {
                    // Scale value suggestions
                    return Arrays.asList("0.5", "1.0", "1.5", "2.0", "2.5", "3.0").stream()
                        .filter(scale -> scale.startsWith(args[3]))
                        .collect(Collectors.toList());
                } else if (Arrays.asList("setline", "insertbefore", "insertafter").contains(subCommand)) {
                    // Text suggestions for line content
                    return Arrays.asList("<red>", "<blue>", "<green>", "<yellow>", "<gold>", "<gray>",
                        "<gradient:", "<bold>", "<italic>", "<underlined>", "Welcome to", "Line text here")
                        .stream()
                        .filter(text -> text.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
            
            // For edit commands
            if ("edit".equals(subCommand)) {
                String editCommand = args[2].toLowerCase();
                
                if ("visibility".equals(editCommand)) {
                    return Arrays.asList("ALL", "MANUAL", "PERMISSION_NEEDED").stream()
                        .filter(type -> type.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                }
                
                if ("billboard".equals(editCommand)) {
                    return Arrays.asList("center", "fixed", "vertical", "horizontal").stream()
                        .filter(type -> type.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                }
                
                if ("textshadow".equals(editCommand)) {
                    return Arrays.asList("true", "false").stream()
                        .filter(type -> type.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                }
                
                if ("textalignment".equals(editCommand)) {
                    return Arrays.asList("center", "left", "right").stream()
                        .filter(type -> type.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                }
                
                if ("background".equals(editCommand)) {
                    return ColorUtils.getColorSuggestions(args[3]);
                }
                
                // Line number completions for edit commands
                if (Arrays.asList("setline", "removeline", "insertbefore", "insertafter",
                    "linescale", "linescalex", "linescaley", "linescalez").contains(editCommand)) {
                    Hologram hologram = plugin.getHologramManager().getHologram(args[1]);
                    if (hologram != null && hologram.getType() == Hologram.HologramType.TEXT) {
                        return getLineNumberCompletions(hologram, editCommand, args[3]);
                    }
                }
            }
        }
        
        if (args.length == 5) {
            String subCommand = args[0].toLowerCase();
            
            // For edit line-specific scale commands
            if ("edit".equals(subCommand)) {
                String editCommand = args[2].toLowerCase();
                if (Arrays.asList("linescale", "linescalex", "linescaley", "linescalez").contains(editCommand)) {
                    // Scale value suggestions
                    return Arrays.asList("0.5", "1.0", "1.5", "2.0", "2.5", "3.0").stream()
                        .filter(scale -> scale.startsWith(args[4]))
                        .collect(Collectors.toList());
                } else if (Arrays.asList("setline", "insertbefore", "insertafter").contains(editCommand)) {
                    // Text suggestions for line content
                    return Arrays.asList("<red>", "<blue>", "<green>", "<yellow>", "<gold>", "<gray>",
                        "<gradient:", "<bold>", "<italic>", "<underlined>", "Welcome to", "Line text here")
                        .stream()
                        .filter(text -> text.toLowerCase().startsWith(args[4].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Helper method to get line number completions for line-specific commands
     */
    private List<String> getLineNumberCompletions(Hologram hologram, String command, String input) {
        List<String> lineNumbers = new ArrayList<>();
        int maxLines = hologram.getTextLines().size();
        
        // For insert commands, allow one more than current lines
        if ("insertbefore".equals(command) || "insertafter".equals(command)) {
            maxLines = Math.max(maxLines, 1); // At least allow line 1 for empty holograms
            if (maxLines > 0) {
                maxLines++; // Allow inserting after the last line
            }
        }
        
        // Ensure at least line 1 exists for most commands
        if (maxLines == 0 && !"removeline".equals(command)) {
            maxLines = 1;
        }
        
        for (int i = 1; i <= maxLines; i++) {
            String lineNum = String.valueOf(i);
            if (lineNum.startsWith(input)) {
                lineNumbers.add(lineNum);
            }
        }
        
        return lineNumbers;
    }
    }
}