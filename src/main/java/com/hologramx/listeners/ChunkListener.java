package com.hologramx.listeners;

import com.hologramx.HologramX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    
    private final HologramX plugin;
    
    public ChunkListener(HologramX plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (plugin.getConfigManager().isChunkLoadingEnabled()) {
            plugin.getHologramManager().onChunkLoad(event.getChunk());
        }
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (plugin.getConfigManager().isChunkLoadingEnabled()) {
            plugin.getHologramManager().onChunkUnload(event.getChunk());
        }
    }
}