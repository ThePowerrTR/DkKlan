package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.tournament.Tournament;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TournamentManager {
    private final DkKlan plugin;
    private final Map<Integer, Tournament> tournaments = new HashMap<>();
    private int nextId = 1;

    public TournamentManager(DkKlan plugin) {
        this.plugin = plugin;
    }

    public Tournament createTournament(int maxTeams) {
        Tournament t = new Tournament(nextId++, maxTeams);
        tournaments.put(t.getId(), t);
        return t;
    }

    public Tournament getTournament(int id) {
        return tournaments.get(id);
    }
    
    public Tournament getActiveTournament() {
        // Return first active
        return tournaments.values().stream().filter(t -> t.isStarted() && !t.isFinished()).findFirst().orElse(null);
    }

    public void handleWarEnd(String winner, String loser) {
        Tournament active = getActiveTournament();
        if (active != null) {
            active.handleMatchResult(winner, loser);
        }
    }
}
