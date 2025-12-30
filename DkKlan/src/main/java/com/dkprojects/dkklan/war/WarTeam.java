package com.dkprojects.dkklan.war;

import org.bukkit.Bukkit;
import java.util.*;

public class WarTeam {
    private final String clanName;
    private final List<UUID> players;
    private final List<UUID> substitutes;
    private int kills;
    private int deaths;
    private int score;

    public WarTeam(String clanName, List<UUID> players) {
        this.clanName = clanName;
        this.players = new ArrayList<>(players);
        this.substitutes = new ArrayList<>();
        this.kills = 0;
        this.deaths = 0;
        this.score = 0;
    }
    
    public List<UUID> getSubstitutes() { return substitutes; }
    
    public boolean swapPlayer(UUID oldPlayer, UUID newPlayer) {
        if (players.contains(oldPlayer)) {
            players.remove(oldPlayer);
            players.add(newPlayer);
            if (!substitutes.contains(oldPlayer)) substitutes.add(oldPlayer);
            if (substitutes.contains(newPlayer)) substitutes.remove(newPlayer);
            return true;
        }
        return false;
    }

    public String getClanName() {
        return clanName;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public boolean hasPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
        this.score++; // Default 1 point per kill
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }
    
    public int getScore() {
        return score;
    }
    
    public void addScore(int amount) {
        this.score += amount;
    }

    public long getOnlineCount() {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .count();
    }
}
