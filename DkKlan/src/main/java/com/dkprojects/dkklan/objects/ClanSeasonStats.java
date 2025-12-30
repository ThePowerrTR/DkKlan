package com.dkprojects.dkklan.objects;

import java.util.UUID;

public class ClanSeasonStats {
    private final String clanName;
    private int wins;
    private int losses;
    private int points;
    private int mvpCount;
    
    public ClanSeasonStats(String clanName) {
        this.clanName = clanName;
    }
    
    public String getClanName() { return clanName; }
    
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public void addWin() { this.wins++; }
    
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public void addLoss() { this.losses++; }
    
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public void addPoints(int points) { this.points += points; }
    
    public int getMvpCount() { return mvpCount; }
    public void setMvpCount(int mvpCount) { this.mvpCount = mvpCount; }
    public void addMvp() { this.mvpCount++; }
    
    public int getGamesPlayed() { return wins + losses; }
    public double getWinRate() {
        if (getGamesPlayed() == 0) return 0.0;
        return (double) wins / getGamesPlayed() * 100.0;
    }
}
