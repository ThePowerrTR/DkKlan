package com.dkprojects.dkklan.tournament;

public class TournamentMatch {
    private final String clanA;
    private final String clanB;
    private String winner;
    private boolean finished;
    private final int bestOf; // 1, 3, 5

    public TournamentMatch(String clanA, String clanB, int bestOf) {
        this.clanA = clanA;
        this.clanB = clanB;
        this.bestOf = bestOf;
        this.finished = false;
    }

    public String getClanA() { return clanA; }
    public String getClanB() { return clanB; }
    public String getWinner() { return winner; }
    public boolean isFinished() { return finished; }
    public int getBestOf() { return bestOf; }

    public void setWinner(String winner) {
        this.winner = winner;
        this.finished = true;
    }
}
