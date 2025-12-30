package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TutorialManager {
    private static TutorialManager instance;
    private final DkKlan plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, String> pending = new HashMap<>();
    private TutorialManager(DkKlan plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tutorials.yml");
        load();
    }
    public static TutorialManager get(DkKlan plugin) {
        if (instance == null) instance = new TutorialManager(plugin);
        return instance;
    }
    private void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
    public boolean shouldShow(UUID uuid, String key) {
        return !config.getBoolean("players." + uuid.toString() + "." + key, false);
    }
    public void markShown(UUID uuid, String key) {
        config.set("players." + uuid.toString() + "." + key, true);
        save();
        pending.remove(uuid);
    }
    public void setPending(UUID uuid, String key) {
        pending.put(uuid, key);
    }
    public String getPending(UUID uuid) {
        return pending.get(uuid);
    }
}
