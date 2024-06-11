package fr.Boulldogo.AssaultPlugin.Utils;

public class FactionRanking {
    private final String factionName;
    private final int points;
    private final int wins;
    private final int losses;

    public FactionRanking(String factionName, int points, int wins, int losses) {
        this.factionName = factionName;
        this.points = points;
        this.wins = wins;
        this.losses = losses;
    }

    public String getFactionName() {
        return factionName;
    }

    public int getPoints() {
        return points;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}
