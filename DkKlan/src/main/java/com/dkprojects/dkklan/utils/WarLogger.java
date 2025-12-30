package com.dkprojects.dkklan.utils;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.war.War;
import com.dkprojects.dkklan.war.WarType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarLogger {
    private final DkKlan plugin;
    private final File logsFolder;
    
    public WarLogger(DkKlan plugin) {
        this.plugin = plugin;
        this.logsFolder = new File(plugin.getDataFolder(), "war_logs");
        if (!logsFolder.exists()) logsFolder.mkdirs();
    }
    
    public void logWar(War war, String winner) {
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String clanA = war.getClanA().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");
        String clanB = war.getClanB().replaceAll("§.", "").replaceAll("[^a-zA-Z0-9]", "");
        String fileName = "war_" + clanA + "_vs_" + clanB + "_" + date + ".yml";
        File file = new File(logsFolder, fileName);
        
        FileConfiguration config = new YamlConfiguration();
        config.set("war-id", UUID.randomUUID().toString().substring(0, 8));
        config.set("date", date);
        config.set("type", war.getType().name());
        config.set("clan-a", war.getClanA());
        config.set("clan-b", war.getClanB());
        config.set("score-a", war.getScoreA());
        config.set("score-b", war.getScoreB());
        config.set("winner", winner != null ? winner : "DRAW");
        config.set("arena", war.getArena().getName());
        config.set("bet", war.getBet());
        
        // Extended Info
        com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getActiveSeries(war.getClanA());
        String format = "Classic";
        if (series != null) {
            format = "BO" + series.getBestOf();
        } else if (war.getType() == WarType.TOURNAMENT) {
            format = "Tournament";
        }
        
        config.set("kit", war.getKitName() != null ? war.getKitName() : "None");
        config.set("format", format);
        config.set("players_locked", war.getRuleset().isAllowSub() ? "false" : "true");
        config.set("inventory_restore", "success");
        
        // Participants
        config.set("team-a-players", war.getTeamA().getPlayers().stream().map(UUID::toString).collect(Collectors.toList()));
        config.set("team-b-players", war.getTeamB().getPlayers().stream().map(UUID::toString).collect(Collectors.toList()));
        
        // MVP
        UUID mvp = null;
        int maxScore = -1;
        for (UUID uuid : war.getAllPlayerStats().keySet()) {
            int score = war.getPlayerStats(uuid).getScore();
            if (score > maxScore) {
                maxScore = score;
                mvp = uuid;
            }
        }
        if (mvp != null) {
            config.set("mvp", mvp.toString());
            config.set("mvp-score", maxScore);
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
