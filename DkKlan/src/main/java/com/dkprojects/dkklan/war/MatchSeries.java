package com.dkprojects.dkklan.war;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchSeries {
    public enum State {
        DRAFTING,
        ACTIVE,
        COMPLETED
    }

    private final String clanA;
    private final String clanB;
    private final int bestOf; // 1, 3, 5
    private final double bet;
    private final int targetPlayerCount;
    private final WarType type;
    private final String kitName;
    
    private int winsA = 0;
    private int winsB = 0;
    private int currentRound = 0;
    
    private War currentWar;
    private final List<War> history = new ArrayList<>();
    
    private List<UUID> rosterA;
    private List<UUID> rosterB;
    
    private MapDraft draft;
    private State state;
    
    public MatchSeries(String clanA, String clanB, int bestOf, double bet, int targetPlayerCount, WarType type, String kitName) {
        this.clanA = clanA;
        this.clanB = clanB;
        this.bestOf = bestOf;
        this.bet = bet;
        this.targetPlayerCount = targetPlayerCount;
        this.type = type;
        this.kitName = kitName;
        this.rosterA = new ArrayList<>();
        this.rosterB = new ArrayList<>();
        this.state = State.ACTIVE; // Default
    }
    
    public void startNextWar(War war) {
        this.currentWar = war;
        this.history.add(war);
        this.currentRound++;
        this.state = State.ACTIVE;
    }
    
    public void handleWarEnd(String winnerClan) {
        if (winnerClan != null) {
            if (winnerClan.equals(clanA)) winsA++;
            else if (winnerClan.equals(clanB)) winsB++;
        }
    }
    
    public boolean isSeriesOver() {
        int targetWins = (bestOf / 2) + 1;
        return winsA >= targetWins || winsB >= targetWins;
    }
    
    public String getSeriesWinner() {
        int targetWins = (bestOf / 2) + 1;
        if (winsA >= targetWins) return clanA;
        if (winsB >= targetWins) return clanB;
        return null;
    }
    
    public String getScoreString() {
        return clanA + " " + winsA + " - " + winsB + " " + clanB;
    }

    public String getClanA() { return clanA; }
    public String getClanB() { return clanB; }
    public int getBestOf() { return bestOf; }
    public double getBet() { return bet; }
    public int getTargetPlayerCount() { return targetPlayerCount; }
    public WarType getType() { return type; }
    public WarType getFormat() { return type; }
    public String getKitName() { return kitName; }
    
    public War getCurrentWar() { return currentWar; }
    public int getWinsA() { return winsA; }
    public int getWinsB() { return winsB; }
    public int getScoreA() { return winsA; }
    public int getScoreB() { return winsB; }
    
    public List<UUID> getRosterA() { return rosterA; }
    public List<UUID> getRosterB() { return rosterB; }
    public void setRosterA(List<UUID> roster) { this.rosterA = roster; }
    public void setRosterB(List<UUID> roster) { this.rosterB = roster; }
    
    public MapDraft getDraft() { return draft; }
    public void setDraft(MapDraft draft) { 
        this.draft = draft; 
        this.state = State.DRAFTING;
    }
    
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
}
