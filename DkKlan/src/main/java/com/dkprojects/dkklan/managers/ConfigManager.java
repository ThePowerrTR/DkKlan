package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Level;

public class ConfigManager {

    private final DkKlan plugin;

    public ConfigManager(DkKlan plugin) {
        this.plugin = plugin;
    }

    public void updateConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        
        // If file doesn't exist, save default and return
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream == null) {
            return;
        }

        FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
        Set<String> keys = defConfig.getKeys(true);
        boolean changed = false;

        for (String key : keys) {
            if (!config.contains(key)) {
                config.set(key, defConfig.get(key));
                changed = true;
                plugin.getLogger().info("Yeni ayar eklendi: " + key);
            }
        }

        if (changed) {
            try {
                config.save(file);
                plugin.getLogger().info(fileName + " güncellendi.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, fileName + " kaydedilemedi!", e);
            }
        }
    }
}
