package com.dkprojects.dkklan.tournament;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.managers.WarManager;
import com.dkprojects.dkklan.war.MatchSeries;
import com.dkprojects.dkklan.war.WarType;
import org.bukkit.Bukkit;

import java.util.*;

public class Tournament {
    private final int id;
    private final int maxTeams;
    private final List<String> participants;
    private final Map<Integer, List<TournamentMatch>> rounds; // Round Number -> Matches
    private int currentRound;
    private boolean started;
    private boolean finished;
    private String champion;

    public Tournament(int id, int maxTeams) {
        this.id = id;
        this.maxTeams = maxTeams;
        this.participants = new ArrayList<>();
        this.rounds = new HashMap<>();
        this.currentRound = 1;
        this.started = false;
        this.finished = false;
    }

    public boolean addParticipant(String clan) {
        if (participants.size() >= maxTeams) return false;
        if (participants.contains(clan)) return false;
        participants.add(clan);
        return true;
    }

    public void start() {
        if (participants.size() < 2) return;
        this.started = true;
        generateBracket();
        startRound(1);
    }

    private void generateBracket() {
        Collections.shuffle(participants);
        List<TournamentMatch> firstRoundMatches = new ArrayList<>();
        
        for (int i = 0; i < participants.size(); i += 2) {
            if (i + 1 < participants.size()) {
                String clanA = participants.get(i);
                String clanB = participants.get(i + 1);
                // Final check handled in startRound usually, but here we just prep matches
                firstRoundMatches.add(new TournamentMatch(clanA, clanB, 1)); // Default BO1
            } else {
                // Bye (automatic win) logic could go here, but for now simple pairs
                // If odd number, last one waits? Simplified: Assume power of 2 for now or handle bye later
            }
        }
        rounds.put(1, firstRoundMatches);
    }

    public void startRound(int round) {
        currentRound = round;
        List<TournamentMatch> matches = rounds.get(round);
        if (matches == null || matches.isEmpty()) {
            endTournament();
            return;
        }

        Bukkit.broadcastMessage("§6🏆 Turnuva " + round + ". Tur Başlıyor! (" + matches.size() + " Maç)");

        // Determine BO format
        // Final is when 1 match remains in the bracket logic eventually
        // For now, simpler logic:
        boolean isFinal = (matches.size() == 1);
        int bo = isFinal ? 5 : (matches.size() == 2 ? 3 : 1); // Semi BO3, Final BO5, others BO1

        for (TournamentMatch match : matches) {
            // We can't auto-start wars because rosters need to be selected.
            // Just announce pairings.
            Bukkit.broadcastMessage("§e⚔ Eşleşme: §f" + match.getClanA() + " §7vs §f" + match.getClanB() + " §e(BO" + bo + ")");
            // In a real system, we'd force invite or open a GUI.
            // Here we can trigger the MatchSeries creation directly if we assume rosters?
            // Or we force them to run a command.
            // Let's force notification.
        }
    }

    public void handleMatchResult(String winner, String loser) {
        if (!started || finished) return;

        List<TournamentMatch> matches = rounds.get(currentRound);
        boolean roundComplete = true;
        List<String> nextRoundParticipants = new ArrayList<>();

        for (TournamentMatch match : matches) {
            if (!match.isFinished()) {
                if ((match.getClanA().equals(winner) && match.getClanB().equals(loser)) ||
                    (match.getClanB().equals(winner) && match.getClanA().equals(loser))) {
                    match.setWinner(winner);
                    // Award points for tournament match win
                    DkKlan.getInstance().getSeasonManager().addPoints(winner, 50);
                    Bukkit.broadcastMessage("§6[Turnuva] §e" + winner + " §7tur atladı! §6+50 Puan");
                }
            }
            
            if (!match.isFinished()) {
                roundComplete = false;
            } else {
                nextRoundParticipants.add(match.getWinner());
            }
        }

        if (roundComplete) {
            if (matches.size() == 1) {
                // Tournament Over
                this.champion = winner;
                endTournament();
            } else {
                // Generate Next Round
                List<TournamentMatch> nextMatches = new ArrayList<>();
                for (int i = 0; i < nextRoundParticipants.size(); i += 2) {
                    if (i + 1 < nextRoundParticipants.size()) {
                        nextMatches.add(new TournamentMatch(nextRoundParticipants.get(i), nextRoundParticipants.get(i+1), 1));
                    }
                }
                rounds.put(currentRound + 1, nextMatches);
                
                Bukkit.getScheduler().runTaskLater(DkKlan.getInstance(), () -> {
                    startRound(currentRound + 1);
                }, 200L); // 10s delay
            }
        }
    }

    private void endTournament() {
        finished = true;
        Bukkit.broadcastMessage("§6🏆 TURNUVA BİTTİ!");
        if (champion != null) {
            Bukkit.broadcastMessage("§a👑 ŞAMPİYON: §l" + champion);
            // DkKlan.getInstance().getSeasonManager().addPoints(champion, 50); // Already awarded per match? Or bonus?
            // Let's give a big bonus for winning the whole thing
            DkKlan.getInstance().getSeasonManager().addPoints(champion, 100); 
            Bukkit.broadcastMessage("§6[Turnuva] §e" + champion + " §7şampiyon oldu! §6+100 Puan");
        }
    }

    public boolean isStarted() { return started; }
    public boolean isFinished() { return finished; }
    public int getId() { return id; }
    public List<String> getParticipants() { return participants; }
}
