package com.hologramx.listeners;

import com.hologramx.HologramX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    
    private final HologramX plugin;
    
    public PlayerListener(HologramX plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Update holograms for the player after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHologramManager().updateHologramsForPlayer(player);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Player quit handling if needed
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only update if player moved to a different block
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            // Update holograms for the player (async to avoid lag)
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHologramManager().updateHologramsForPlayer(event.getPlayer());
                }
            }.runTaskAsynchronously(plugin);
        }
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Update holograms when player changes world
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHologramManager().updateHologramsForPlayer(player);
            }
        }.runTaskLater(plugin, 5L);
    }
}