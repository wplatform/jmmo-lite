package com.github.azeroth.game.arena;


public final class ArenaTeamStats {
    public short rating;
    public short weekGames;
    public short weekWins;
    public short seasonGames;
    public short seasonWins;
    public int rank;

    public ArenaTeamStats clone() {
        ArenaTeamStats varCopy = new arenaTeamStats();

        varCopy.rating = this.rating;
        varCopy.weekGames = this.weekGames;
        varCopy.weekWins = this.weekWins;
        varCopy.seasonGames = this.seasonGames;
        varCopy.seasonWins = this.seasonWins;
        varCopy.rank = this.rank;

        return varCopy;
    }
}
