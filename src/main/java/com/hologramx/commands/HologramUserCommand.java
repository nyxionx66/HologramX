package com.hologramx.commands;

import com.hologramx.HologramX;
import com.hologramx.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HologramUserCommand implements CommandExecutor, TabCompleter {
    
    private final HologramX plugin;
    
    public HologramUserCommand(HologramX plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list" -> handleList(player, args);
            case "info" -> handleInfo(player, args);
            case "near" -> handleNear(player, args);
            case "tp", "teleport" -> handleTeleport(player, args);
            default -> sendUsage(player);
        }
        
        return true;
    }
    
    private void handleList(Player player, String[] args) {
        if (!player.hasPermission("hologramx.user")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        Collection<Hologram> allHolograms = plugin.getHologramManager().getHolograms();
        List<Hologram> visibleHolograms = allHolograms.stream()
            .filter(hologram -> hologram.canView(player))
            .collect(Collectors.toList());
        
        if (visibleHolograms.isEmpty()) {
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
        int maxPages = (int) Math.ceil((double) visibleHolograms.size() / perPage);
        page = Math.max(1, Math.min(page, maxPages));
        
        plugin.getMessages().sendMessage(player, "hologram-list-header", 
            "page", String.valueOf(page), "max_pages", String.valueOf(maxPages));
        
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, visibleHolograms.size());
        
        for (int i = start; i < end; i++) {
            Hologram hologram = visibleHolograms.get(i);
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
    
    private void handleInfo(Player player, String[] args) {
        if (!player.hasPermission("hologramx.user")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/holo info <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        if (!hologram.canView(player)) {
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
        
        double distance = loc != null ? player.getLocation().distance(loc) : 0;
        plugin.getMessages().sendMessageWithoutPrefix(player, 
            "<gray>Distance: <white>" + String.format("%.1f", distance) + " blocks</white></gray>");
    }
    
    private void handleNear(Player player, String[] args) {
        if (!player.hasPermission("hologramx.user")) {
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
        
        // Filter by visibility
        nearby = nearby.stream()
            .filter(hologram -> hologram.canView(player))
            .collect(Collectors.toList());
        
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
    
    private void handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("hologramx.user")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        if (args.length < 2) {
            plugin.getMessages().sendMessage(player, "invalid-syntax", 
                "usage", "/holo tp <name>");
            return;
        }
        
        String name = args[1];
        Hologram hologram = plugin.getHologramManager().getHologram(name);
        
        if (hologram == null || !hologram.canView(player)) {
            plugin.getMessages().sendMessage(player, "hologram-not-found", "name", name);
            return;
        }
        
        // Check permission for specific hologram
        if (!player.hasPermission("hologramx.interact." + name) && 
            !player.hasPermission("hologramx.interact.*")) {
            plugin.getMessages().sendMessage(player, "no-permission");
            return;
        }
        
        Location location = hologram.getLocation();
        if (location != null) {
            player.teleport(location);
            plugin.getMessages().sendMessage(player, "hologram-teleported", "name", name);
        }
    }
    
    private void sendUsage(Player player) {
        plugin.getMessages().sendMessageWithoutPrefix(player, "<yellow>Hologram User Commands:</yellow>");
        plugin.getMessages().sendMessageWithoutPrefix(player, "<gray>/holo list [page] <dark_gray>- List visible holograms</dark_gray>");
        plugin.getMessages().sendMessageWithoutPrefix(player, "<gray>/holo info <name> <dark_gray>- Show hologram info</dark_gray>");
        plugin.getMessages().sendMessageWithoutPrefix(player, "<gray>/holo near [distance] <dark_gray>- Show nearby holograms</dark_gray>");
        plugin.getMessages().sendMessageWithoutPrefix(player, "<gray>/holo tp <name> <dark_gray>- Teleport to hologram</dark_gray>");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player player)) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("list", "info", "near", "tp");
            return commands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (Arrays.asList("info", "tp").contains(subCommand)) {
                return plugin.getHologramManager().getHolograms().stream()
                    .filter(hologram -> hologram.canView(player))
                    .map(Hologram::getId)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}