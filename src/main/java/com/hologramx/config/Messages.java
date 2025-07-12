package com.hologramx.config;

import com.hologramx.HologramX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    
    private final HologramX plugin;
    private final File messagesFile;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messageCache = new HashMap<>();
    
    public Messages(HologramX plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }
    
    public void load() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        cacheMessages();
    }
    
    public void reload() {
        messageCache.clear();
        load();
    }
    
    private void cacheMessages() {
        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messageCache.put(key, messagesConfig.getString(key));
            }
        }
    }
    
    public String getRawMessage(String key) {
        return messageCache.getOrDefault(key, key);
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getRawMessage(key);
        
        // Apply placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    public Component getComponent(String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        return MiniMessage.miniMessage().deserialize(message);
    }
    
    public void sendMessage(Player player, String key, String... placeholders) {
        String prefix = getRawMessage("prefix");
        String message = getMessage(key, placeholders);
        Component component = MiniMessage.miniMessage().deserialize(prefix + message);
        player.sendMessage(component);
    }
    
    public void sendMessageWithoutPrefix(Player player, String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = MiniMessage.miniMessage().deserialize(message);
        player.sendMessage(component);
    }
}