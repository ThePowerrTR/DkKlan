package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import org.bukkit.Bukkit;

public class EloManager {
    private final DkKlan plugin;
    private int kFactor;
    private int winBonus;
    private int lossPenalty;

    public EloManager(DkKlan plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        kFactor = plugin.getConfig().getInt("elo.k-factor", 32);
        winBonus = plugin.getConfig().getInt("elo.win-bonus", 10);
        lossPenalty = plugin.getConfig().getInt("elo.loss-penalty", 5);
    }

    public void calculateAndApplyElo(String winnerClan, String loserClan) {
        // Anti-Farming Check
        // Check if they fought in the last 6 hours (21600000 ms)
        int recentMatches = plugin.getKlanManager().getRecentMatchCount(winnerClan, loserClan, 21600000L);
        double multiplier = 1.0;
        
        if (recentMatches >= 2) {
            multiplier = 0.0;
            Bukkit.getLogger().info("[DkKlan] ELO Farming detected (Repeated) between " + winnerClan + " and " + loserClan + ". ELO gain set to 0.");
        } else if (recentMatches == 1) {
            multiplier = 0.5;
            Bukkit.getLogger().info("[DkKlan] ELO Farming detected (Once) between " + winnerClan + " and " + loserClan + ". ELO gain reduced by 50%.");
        }

        int eloWinner = plugin.getKlanManager().getKlanElo(winnerClan);
        int eloLoser = plugin.getKlanManager().getKlanElo(loserClan);

        // Simple ELO formula
        double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (eloLoser - eloWinner) / 400.0));
        
        int change = (int) (kFactor * (1 - expectedWinner) * multiplier);
        
        int newWinnerElo = eloWinner + change + winBonus;
        int newLoserElo = Math.max(0, eloLoser - change - lossPenalty); // No negative ELO

        plugin.getKlanManager().setKlanElo(winnerClan, newWinnerElo);
        plugin.getKlanManager().setKlanElo(loserClan, newLoserElo);

        updateLeague(winnerClan, newWinnerElo);
        updateLeague(loserClan, newLoserElo);
    }

    private void updateLeague(String clan, int elo) {
        // Simple iteration over config to find league
        // This is a placeholder logic, ideally reading from 'leagues' section map
        String newLeague = "Bronze";
        if (elo >= 1800) newLeague = "Master";
        else if (elo >= 1500) newLeague = "Diamond";
        else if (elo >= 1300) newLeague = "Gold";
        else if (elo >= 1100) newLeague = "Silver";
        
        plugin.getKlanManager().setKlanLeague(clan, newLeague);
    }
}
