package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.objects.ClanSeasonStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SeasonManager {
    private final DkKlan plugin;
    private final Map<String, ClanSeasonStats> stats = new HashMap<>();
    private final File statsFile;
    private FileConfiguration statsConfig;
    
    // Player MVP tracking
    private final Map<UUID, Integer> playerMvps = new HashMap<>();

    public SeasonManager(DkKlan plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "season_stats.yml");
        loadStats();
    }

    private void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        
        if (statsConfig.contains("clans")) {
            for (String clan : statsConfig.getConfigurationSection("clans").getKeys(false)) {
                String path = "clans." + clan;
                ClanSeasonStats stat = new ClanSeasonStats(clan);
                stat.setWins(statsConfig.getInt(path + ".wins"));
                stat.setLosses(statsConfig.getInt(path + ".losses"));
                stat.setPoints(statsConfig.getInt(path + ".points"));
                stat.setMvpCount(statsConfig.getInt(path + ".mvpCount"));
                stats.put(clan, stat);
            }
        }
        
        if (statsConfig.contains("players")) {
            for (String uuidStr : statsConfig.getConfigurationSection("players").getKeys(false)) {
                playerMvps.put(UUID.fromString(uuidStr), statsConfig.getInt("players." + uuidStr + ".mvps"));
            }
        }
    }

    public void saveStats() {
        for (ClanSeasonStats stat : stats.values()) {
            String path = "clans." + stat.getClanName();
            statsConfig.set(path + ".wins", stat.getWins());
            statsConfig.set(path + ".losses", stat.getLosses());
            statsConfig.set(path + ".points", stat.getPoints());
            statsConfig.set(path + ".mvpCount", stat.getMvpCount());
        }
        
        for (Map.Entry<UUID, Integer> entry : playerMvps.entrySet()) {
            statsConfig.set("players." + entry.getKey().toString() + ".mvps", entry.getValue());
        }
        
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClanSeasonStats getStats(String clan) {
        return stats.computeIfAbsent(clan, ClanSeasonStats::new);
    }

    public void addPoints(String clan, int points) {
        getStats(clan).addPoints(points);
        saveStats();
    }

    public void addWin(String clan) {
        getStats(clan).addWin();
        saveStats();
    }

    public void addLoss(String clan) {
        getStats(clan).addLoss();
        saveStats();
    }
    
    public void addMvp(UUID player) {
        playerMvps.merge(player, 1, Integer::sum);
        
        // Also update clan stats if player is in a clan
        String clan = plugin.getKlanManager().getPlayerKlan(player);
        if (clan != null) {
            getStats(clan).addMvp();
            saveStats();
        }
    }
    
    public List<ClanSeasonStats> getTopClans(int limit) {
        return stats.values().stream()
                .sorted((c1, c2) -> Integer.compare(c2.getPoints(), c1.getPoints()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public int getPlayerMvps(UUID player) {
        return playerMvps.getOrDefault(player, 0);
    }
}
